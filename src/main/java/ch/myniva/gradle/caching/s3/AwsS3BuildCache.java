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

package ch.myniva.gradle.caching.s3;

import org.gradle.api.Action;
import org.gradle.api.credentials.AwsCredentials;
import org.gradle.caching.configuration.AbstractBuildCache;

public class AwsS3BuildCache extends AbstractBuildCache {

  private AwsCredentials credentials;
  private String region;
  private String bucket;

  /**
   * Returns the credentials used to access the AWS S3 bucket.
   */
  public AwsCredentials getCredentials() {
    return credentials;
  }

  /**
   * Configures the credentials used to access the AWS S3 bucket.
   */
  public void credentials(Action<? super AwsCredentials> configuration) {
    configuration.execute(credentials);
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }
}
