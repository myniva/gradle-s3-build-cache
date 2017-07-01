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

import static com.amazonaws.util.StringUtils.isNullOrEmpty;

import ch.myniva.gradle.caching.s3.AwsS3BuildCache;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.gradle.api.GradleException;
import org.gradle.caching.BuildCacheService;
import org.gradle.caching.BuildCacheServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsS3BuildCacheServiceFactory implements BuildCacheServiceFactory<AwsS3BuildCache> {

  private static final Logger logger = LoggerFactory.getLogger(AwsS3BuildCacheServiceFactory.class);

  @Override
  public BuildCacheService createBuildCacheService(AwsS3BuildCache config, Describer describer) {
    logger.debug("Start creating S3 build cache service");

    describer
        .type("AWS S3")
        .config("AWS region", config.getRegion())
        .config("Bucket", config.getBucket());

    verifyConfig(config);
    AmazonS3 s3 = createS3Client(config);

    return new AwsS3BuildCacheService(s3, config.getBucket());
  }

  private void verifyConfig(AwsS3BuildCache config) {
    if (isNullOrEmpty(config.getRegion())) {
      throw new IllegalStateException("S3 build cache has no AWS region configured");
    }
    if (isNullOrEmpty(config.getBucket())) {
      throw new IllegalStateException("S3 build cache has no bucket configured");
    }
  }

  private AmazonS3 createS3Client(AwsS3BuildCache config) {
    AmazonS3 s3;
    try {
      s3 = AmazonS3ClientBuilder
          .standard()
          .withRegion(config.getRegion())
          .build();
    } catch (SdkClientException e) {
      logger.debug("Error while building AWS S3 client: {}", e.getMessage());
      throw new GradleException("Creation of S3 build cache failed; cannot create S3 client", e);
    }
    return s3;
  }
}
