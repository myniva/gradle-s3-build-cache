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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.StorageClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.gradle.caching.BuildCacheEntryReader;
import org.gradle.caching.BuildCacheEntryWriter;
import org.gradle.caching.BuildCacheException;
import org.gradle.caching.BuildCacheKey;
import org.gradle.caching.BuildCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsS3BuildCacheService implements BuildCacheService {

  private static final String BUILD_CACHE_CONTENT_TYPE = "application/vnd.gradle.build-cache-artifact";
  private static final Logger logger = LoggerFactory.getLogger(AwsS3BuildCacheService.class);

  private final AmazonS3 s3;
  private final String bucketName;
  private final String path;
  private final boolean reducedRedundancy;

  AwsS3BuildCacheService(AmazonS3 s3, String bucketName, String path, boolean reducedRedundancy) {
    this.s3 = s3;
    this.bucketName = bucketName;
    this.path = path;
    this.reducedRedundancy = reducedRedundancy;
  }

  private String getBucketPath(BuildCacheKey key) {
    if (path == null || path.length() == 0) {
      return key.getHashCode();
    }
    return (path+"/"+key.getHashCode()).replaceAll("[/]+", "/");
  }

  @Override
  public boolean load(BuildCacheKey key, BuildCacheEntryReader reader) {
    final String bucketPath = getBucketPath(key);
    if (s3.doesObjectExist(bucketName, bucketPath)) {
      logger.info("Found cache item '{}' in S3 bucket", bucketPath);
      S3Object object = s3.getObject(bucketName, bucketPath);
      try (InputStream is = object.getObjectContent()) {
        reader.readFrom(is);
        return true;
      } catch (IOException e) {
        throw new BuildCacheException("Error while reading cache object from S3 bucket", e);
      }
    } else {
      logger.info("Did not find cache item '{}' in S3 bucket", bucketPath);
      return false;
    }
  }

  @Override
  public void store(BuildCacheKey key, BuildCacheEntryWriter writer) {
    final String bucketPath = getBucketPath(key);
    logger.info("Start storing cache entry '{}' in S3 bucket", bucketPath);
    ObjectMetadata meta = new ObjectMetadata();
    meta.setContentType(BUILD_CACHE_CONTENT_TYPE);

    File tempOutputFile;
    try {
      tempOutputFile = File.createTempFile("gradle-s3-build-cache", ".tmp");
    } catch (IOException e) {
      throw new BuildCacheException("Error writing temp file", e);
    }

    try (FileOutputStream os = new FileOutputStream(tempOutputFile)) {
      writer.writeTo(os);
      meta.setContentLength(tempOutputFile.length());
      try (InputStream is = new FileInputStream(tempOutputFile)) {
          PutObjectRequest request = getPutObjectRequest(bucketPath, meta, is);
          if(this.reducedRedundancy) {
            request.withStorageClass(StorageClass.ReducedRedundancy);
          }
          s3.putObject(request);
      }
    } catch (IOException e) {
      throw new BuildCacheException("Error while storing cache object in S3 bucket", e);
    } finally {
      tempOutputFile.delete();
    }
  }

  protected PutObjectRequest getPutObjectRequest(String bucketPath, ObjectMetadata meta, InputStream is) {
    return new PutObjectRequest(bucketName, bucketPath, is, meta);
  }

  @Override
  public void close() throws IOException {
    // The AWS S3 client does not need to be closed
  }
}
