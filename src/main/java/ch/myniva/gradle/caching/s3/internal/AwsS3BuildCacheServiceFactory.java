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

import ch.myniva.gradle.caching.s3.AwsS3BuildCache;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.gradle.caching.BuildCacheService;
import org.gradle.caching.BuildCacheServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsS3BuildCacheServiceFactory implements BuildCacheServiceFactory<AwsS3BuildCache> {
  Logger logger = LoggerFactory.getLogger(AwsS3BuildCacheServiceFactory.class);

  @Override
  public BuildCacheService createBuildCacheService(AwsS3BuildCache config) {
    AmazonS3 s3 = AmazonS3ClientBuilder
        .standard()
        .withRegion(config.getRegion())
        .build();

    try {
      s3.getBucketPolicy(config.getBucket());
    } catch (AmazonServiceException e) {
      logger.warn("error code: {}", e.getErrorCode());
      throw e;
    }

    return new AwsS3BuildCacheService(s3, config.getBucket());
  }
}
