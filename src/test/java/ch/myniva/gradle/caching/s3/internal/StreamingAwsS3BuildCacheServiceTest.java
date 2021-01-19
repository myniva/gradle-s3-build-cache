/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.myniva.gradle.caching.s3.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import org.gradle.caching.BuildCacheEntryReader;
import org.gradle.caching.BuildCacheEntryWriter;
import org.gradle.caching.BuildCacheKey;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class StreamingAwsS3BuildCacheServiceTest {
  @Mock
  AmazonS3 s3;
  @Mock
  BuildCacheKey key;
  @Mock
  BuildCacheEntryReader reader;

  BuildCacheEntryWriter smallFileWriter = new BuildCacheEntryWriter() {
    @Override
    public void writeTo(OutputStream output) throws IOException {
      output.write("test data".getBytes());
    }
  };

  BuildCacheEntryWriter largeFileWriter = new BuildCacheEntryWriter() {
    Random random = new Random();
    @Override
    public void writeTo(OutputStream output) throws IOException {
      for (int x = 0; x < 6; x++) {
        byte[] bytes = new byte[1000000];
        random.nextBytes(bytes);
        output.write(bytes);
      }
    }
  };

  @Mock
  PutObjectRequest putObjectRequest = mock(PutObjectRequest.class);

  AwsS3BuildCacheService buildCacheService;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    doReturn("abcdefghijkl123456789").when(key).getHashCode();
  }

  @Test
  public void loadGetsObjectsAndReturnsTrueIfItExistsInS3() throws Exception {
    /** Setup **/
    buildCacheService = new StreamingAwsS3BuildCacheService(s3, "bucketName", null, true);
    doReturn(true).when(s3).doesObjectExist("bucketName", "abcdefghijkl123456789");
    S3Object s3Object = mock(S3Object.class);
    doReturn(s3Object).when(s3).getObject("bucketName", "abcdefghijkl123456789");
    S3ObjectInputStream s3ObjectInputStream = mock(S3ObjectInputStream.class);
    doReturn(s3ObjectInputStream).when(s3Object).getObjectContent();

    /** Run **/
    boolean result = buildCacheService.load(key, reader);

    /** Check **/
    assertTrue(result);
    verify(reader).readFrom(s3ObjectInputStream);
  }

  @Test
  public void loadReturnsFalseIfItDoesntExistInS3() throws Exception {
    /** Setup **/
    buildCacheService = new StreamingAwsS3BuildCacheService(s3, "bucketName", null, true);
    doReturn(false).when(s3).doesObjectExist("bucketName", "abcdefghijkl123456789");

    /** Run **/
    boolean result = buildCacheService.load(key, reader);

    /** Check **/
    assertFalse(result);
  }

  @Test
  public void storePutsObjectAndUsesReducedRedundancyWhenConfigured() throws IOException {
    /** Setup **/
    buildCacheService = spy(new StreamingAwsS3BuildCacheService(s3, "bucketName", null, true));

    /** Run **/
    buildCacheService.store(key, smallFileWriter);

    /** Check **/
    final ArgumentCaptor<PutObjectRequest> captor =
            ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3).putObject(captor.capture());
    assertEquals(StorageClass.ReducedRedundancy.toString(), captor.getValue().getStorageClass());
    assertEquals("bucketName", captor.getValue().getBucketName());
    assertEquals(key.getHashCode(), captor.getValue().getKey());
  }

  @Test
  public void storePutsObjectAndDoesNotUseReducedRedundancyWhenConfigured() throws IOException {
    /** Setup **/
    buildCacheService = spy(new StreamingAwsS3BuildCacheService(s3, "bucketName", null, false));

    /** Run **/
    buildCacheService.store(key, smallFileWriter);

    /** Check **/
    final ArgumentCaptor<PutObjectRequest> captor =
            ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3).putObject(captor.capture());
    assertEquals(StorageClass.Standard.toString(), captor.getValue().getStorageClass());
    assertEquals("bucketName", captor.getValue().getBucketName());
    assertEquals(key.getHashCode(), captor.getValue().getKey());
  }

  @Test
  public void storePutsObjectAndUsesPathWhenConfigured() throws IOException {
    /** Setup **/
    buildCacheService = spy(new StreamingAwsS3BuildCacheService(s3, "bucketName", "cache", false));

    /** Run **/
    buildCacheService.store(key, smallFileWriter);

    /** Check **/
    final ArgumentCaptor<PutObjectRequest> captor =
            ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3).putObject(captor.capture());
    assertEquals(StorageClass.Standard.toString(), captor.getValue().getStorageClass());
    assertEquals("bucketName", captor.getValue().getBucketName());
    assertEquals("cache/" + key.getHashCode(), captor.getValue().getKey());
  }

  @Test
  public void storeUploadsObjectAndUsesReducedRedundancyWhenConfigured() throws IOException {
    /** Setup **/
    buildCacheService = spy(new StreamingAwsS3BuildCacheService(s3, "bucketName", null, true));
    InitiateMultipartUploadResult initialResult = new InitiateMultipartUploadResult();
    initialResult.setUploadId("upload-id");
    when(s3.initiateMultipartUpload(any(InitiateMultipartUploadRequest.class))).thenReturn(initialResult);

    UploadPartResult uploadPartResult = new UploadPartResult();
    uploadPartResult.setETag("etag");
    when(s3.uploadPart(any(UploadPartRequest.class))).thenReturn(uploadPartResult);

    /** Run **/
    buildCacheService.store(key, largeFileWriter);

    /** Check **/
    final ArgumentCaptor<InitiateMultipartUploadRequest> captor =
            ArgumentCaptor.forClass(InitiateMultipartUploadRequest.class);
    verify(s3).initiateMultipartUpload(captor.capture());

    final ArgumentCaptor<CompleteMultipartUploadRequest> completeCaptor =
            ArgumentCaptor.forClass(CompleteMultipartUploadRequest.class);
    verify(s3).completeMultipartUpload(completeCaptor.capture());

    assertEquals(StorageClass.ReducedRedundancy, captor.getValue().getStorageClass());
    assertEquals("bucketName", captor.getValue().getBucketName());
    assertEquals(key.getHashCode(), completeCaptor.getValue().getKey());
  }

  public void storeUploadsObjectAndDoesNotUseReducedRedundancyWhenConfigured() throws IOException {
    /** Setup **/
    buildCacheService = spy(new StreamingAwsS3BuildCacheService(s3, "bucketName", null, false));
    InitiateMultipartUploadResult initialResult = new InitiateMultipartUploadResult();
    initialResult.setUploadId("upload-id");
    when(s3.initiateMultipartUpload(any(InitiateMultipartUploadRequest.class))).thenReturn(initialResult);

    UploadPartResult uploadPartResult = new UploadPartResult();
    uploadPartResult.setETag("etag");
    when(s3.uploadPart(any(UploadPartRequest.class))).thenReturn(uploadPartResult);

    /** Run **/
    buildCacheService.store(key, largeFileWriter);

    /** Check **/
    final ArgumentCaptor<InitiateMultipartUploadRequest> captor =
            ArgumentCaptor.forClass(InitiateMultipartUploadRequest.class);
    verify(s3).initiateMultipartUpload(captor.capture());

    final ArgumentCaptor<CompleteMultipartUploadRequest> completeCaptor =
            ArgumentCaptor.forClass(CompleteMultipartUploadRequest.class);
    verify(s3).completeMultipartUpload(completeCaptor.capture());

    assertEquals(StorageClass.Standard, captor.getValue().getStorageClass());
    assertEquals("bucketName", captor.getValue().getBucketName());
    assertEquals(key.getHashCode(), completeCaptor.getValue().getKey());
  }

  public void storeUploadsObjectAndPathWhenConfigured() throws IOException {
    /** Setup **/
    buildCacheService = spy(new StreamingAwsS3BuildCacheService(s3, "bucketName", "cache", true));
    InitiateMultipartUploadResult initialResult = new InitiateMultipartUploadResult();
    initialResult.setUploadId("upload-id");
    when(s3.initiateMultipartUpload(any(InitiateMultipartUploadRequest.class))).thenReturn(initialResult);

    UploadPartResult uploadPartResult = new UploadPartResult();
    uploadPartResult.setETag("etag");
    when(s3.uploadPart(any(UploadPartRequest.class))).thenReturn(uploadPartResult);

    /** Run **/
    buildCacheService.store(key, largeFileWriter);

    /** Check **/
    final ArgumentCaptor<InitiateMultipartUploadRequest> captor =
            ArgumentCaptor.forClass(InitiateMultipartUploadRequest.class);
    verify(s3).initiateMultipartUpload(captor.capture());

    final ArgumentCaptor<CompleteMultipartUploadRequest> completeCaptor =
            ArgumentCaptor.forClass(CompleteMultipartUploadRequest.class);
    verify(s3).completeMultipartUpload(completeCaptor.capture());

    assertEquals(StorageClass.ReducedRedundancy, captor.getValue().getStorageClass());
    assertEquals("bucketName", captor.getValue().getBucketName());
    assertEquals("cache/" + key.getHashCode(), completeCaptor.getValue().getKey());
  }

}
