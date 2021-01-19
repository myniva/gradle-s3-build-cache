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
import org.gradle.caching.BuildCacheEntryWriter;
import org.gradle.caching.BuildCacheException;
import org.gradle.caching.BuildCacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class StreamingAwsS3BuildCacheService extends AwsS3BuildCacheService {

  private static final String BUILD_CACHE_CONTENT_TYPE = "application/vnd.gradle.build-cache-artifact";
  private static final Logger logger = LoggerFactory.getLogger(StreamingAwsS3BuildCacheService.class);

  private final AmazonS3 s3;
  private final String bucketName;
  private final String path;
  private final boolean reducedRedundancy;

  StreamingAwsS3BuildCacheService(AmazonS3 s3, String bucketName, String path, boolean reducedRedundancy) {
    super(s3, bucketName, path, reducedRedundancy);

    this.s3 = s3;
    this.bucketName = bucketName;
    this.path = path;
    this.reducedRedundancy = reducedRedundancy;
  }

  @Override
  public void store(BuildCacheKey key, BuildCacheEntryWriter writer) {
    final String bucketPath = getBucketPath(key);
    logger.info("Start storing cache entry '{}' in S3 bucket", bucketPath);
    ObjectMetadata meta = new ObjectMetadata();
    meta.setContentType(BUILD_CACHE_CONTENT_TYPE);

    try (S3OutputStream os = new S3OutputStream(s3, bucketName, bucketPath, reducedRedundancy)) {
      writer.writeTo(os);
      meta.setContentLength(os.getBytesWritten());

    } catch (IOException e) {
      throw new BuildCacheException("Error while storing cache object in S3 bucket", e);
    }
  }

}
