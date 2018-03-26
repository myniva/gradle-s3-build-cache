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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.StorageClass;
import org.gradle.caching.BuildCacheEntryWriter;
import org.gradle.caching.BuildCacheKey;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AwsS3BuildCacheServiceTest {
  @Mock
  AmazonS3 s3;
  @Mock
  BuildCacheKey key;
  @Mock
  BuildCacheEntryWriter writer;
  @Mock
  PutObjectRequest putObjectRequest = mock(PutObjectRequest.class);

  AwsS3BuildCacheService buildCacheService;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    doReturn("abcdefghijkl123456789").when(key).getHashCode();
  }

  @Test
  public void storePutsObjectAndUsesReducedRedundancyWhenConfigured() throws IOException {
    /** Setup **/
    buildCacheService = spy(new AwsS3BuildCacheService(s3, "bucketName", null, true));
    doReturn(putObjectRequest).when(buildCacheService).getPutObjectRequest(any(String.class),
            any(ObjectMetadata.class), any(InputStream.class));

    /** Run **/
    buildCacheService.store(key, writer);

    /** Check **/
    verifyThatStoreStores("abcdefghijkl123456789");
    verify(putObjectRequest).withStorageClass(eq(StorageClass.ReducedRedundancy));
  }

  @Test
  public void storePutsObjectAndDoesNotUseReducedRedundancyWhenConfigured() throws IOException {
    /** Setup **/
    buildCacheService = spy(new AwsS3BuildCacheService(s3, "bucketName", null, false));
    doReturn(putObjectRequest).when(buildCacheService).getPutObjectRequest(any(String.class),
            any(ObjectMetadata.class), any(InputStream.class));

    /** Run **/
    buildCacheService.store(key, writer);

    /** Check **/
    verifyThatStoreStores("abcdefghijkl123456789");
    verify(putObjectRequest, never()).withStorageClass(eq(StorageClass.ReducedRedundancy));
  }

  @Test
  public void storePutsObjectAndUsesPathWhenConfigured() throws IOException {
    /** Setup **/
    buildCacheService = spy(new AwsS3BuildCacheService(s3, "bucketName", "cache", false));
    doReturn(putObjectRequest).when(buildCacheService).getPutObjectRequest(eq("cache/abcdefghijkl123456789"),
            any(ObjectMetadata.class), any(InputStream.class));

    /** Run **/
    buildCacheService.store(key, writer);

    /** Check **/
    verifyThatStoreStores("cache/abcdefghijkl123456789");
    verify(putObjectRequest, never()).withStorageClass(eq(StorageClass.ReducedRedundancy));
  }

  private void verifyThatStoreStores(String bucketPath) throws IOException {
    verify(writer).writeTo(any(ByteArrayOutputStream.class));
    verify(buildCacheService).getPutObjectRequest(eq(bucketPath), any(ObjectMetadata.class), any(InputStream.class));
    verify(s3).putObject(eq(putObjectRequest));
  }
}
