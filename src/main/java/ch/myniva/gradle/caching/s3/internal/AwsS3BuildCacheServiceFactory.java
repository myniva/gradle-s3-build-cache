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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.util.Map;
import org.gradle.api.GradleException;
import org.gradle.caching.BuildCacheService;
import org.gradle.caching.BuildCacheServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.myniva.gradle.caching.s3.AwsS3BuildCache;

public class AwsS3BuildCacheServiceFactory implements BuildCacheServiceFactory<AwsS3BuildCache> {

  private static final Logger logger = LoggerFactory.getLogger(AwsS3BuildCacheServiceFactory.class);

  @Override
  public BuildCacheService createBuildCacheService(AwsS3BuildCache config, Describer describer) {
    logger.debug("Start creating S3 build cache service");

    describer
        .type("AWS S3")
        .config("Region", config.getRegion())
        .config("Bucket", config.getBucket())
        .config("Reduced Redundancy", String.valueOf(config.isReducedRedundancy()));

    if (config.getPath() != null) {
      describer.config("Path", config.getPath());
    }

    if (config.getEndpoint() != null) {
      describer.config("Endpoint", config.getEndpoint());
    }

    verifyConfig(config);
    AmazonS3 s3 = createS3Client(config);

    return new AwsS3BuildCacheService(s3, config.getBucket(), config.getPath(), config.isReducedRedundancy());
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
      AmazonS3ClientBuilder s3Builder = AmazonS3ClientBuilder.standard();
      if (!isNullOrEmpty(config.getAwsAccessKeyId()) && !isNullOrEmpty(config.getAwsSecretKey()) &&
                !isNullOrEmpty(config.getSessionToken())) {
            s3Builder.withCredentials(new AWSStaticCredentialsProvider(
                    new BasicSessionCredentials(config.getAwsAccessKeyId(), config.getAwsSecretKey(),
                            config.getSessionToken())));
      } else if (!isNullOrEmpty(config.getAwsAccessKeyId()) && !isNullOrEmpty(config.getAwsSecretKey())) {
        s3Builder.withCredentials(new AWSStaticCredentialsProvider(
            new BasicAWSCredentials(config.getAwsAccessKeyId(), config.getAwsSecretKey())));
      }

      addHttpHeaders(s3Builder, config);

      if (isNullOrEmpty(config.getEndpoint())) {
        s3Builder.withRegion(config.getRegion());
      } else {
        s3Builder.withEndpointConfiguration(
            new AwsClientBuilder.EndpointConfiguration(config.getEndpoint(), config.getRegion()));
      }
      s3 = s3Builder.build();
    } catch (SdkClientException e) {
      logger.debug("Error while building AWS S3 client: {}", e.getMessage());
      throw new GradleException("Creation of S3 build cache failed; cannot create S3 client", e);
    }
    return s3;
  }

  private void addHttpHeaders(final AmazonS3ClientBuilder s3Builder, final AwsS3BuildCache config) {
    final Map<String, String> headers = config.getHeaders();
    if (headers != null) {
      final ClientConfiguration clientConfiguration = new ClientConfiguration();
      for (Map.Entry<String, String> header : headers.entrySet()) {
        if(header.getKey() != null && header.getValue() != null) {
          clientConfiguration.addHeader(header.getKey(), header.getValue());
        }
      }
      s3Builder.setClientConfiguration(clientConfiguration);
    }
  }
}
