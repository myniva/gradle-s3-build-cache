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

import static org.junit.Assert.assertNotNull;

import ch.myniva.gradle.caching.s3.AwsS3BuildCache;
import java.util.HashMap;
import java.util.Map;
import org.gradle.caching.BuildCacheService;
import org.gradle.caching.BuildCacheServiceFactory.Describer;
import org.junit.Before;
import org.junit.Test;

public class AwsS3BuildCacheServiceFactoryTest {

  private AwsS3BuildCacheServiceFactory subject;
  private Describer buildCacheDescriber;

  @Before
  public void setUp() {
    subject = new AwsS3BuildCacheServiceFactory();
    buildCacheDescriber = new NoopBuildCacheDescriber();
  }

  @Test
  public void testWhat() {
    AwsS3BuildCache conf = new AwsS3BuildCache();
    conf.setRegion("us-west-1");
    conf.setBucket("my-bucket");

    BuildCacheService service = subject.createBuildCacheService(conf, buildCacheDescriber);

    assertNotNull(service);
  }

  @Test
  public void testPath() {
    AwsS3BuildCache conf = new AwsS3BuildCache();
    conf.setRegion("us-west-1");
    conf.setBucket("my-bucket");
    conf.setPath("cache");

    BuildCacheService service = subject.createBuildCacheService(conf, buildCacheDescriber);

    assertNotNull(service);
  }

  @Test
  public void testNullHeaders() {
    AwsS3BuildCache conf = new AwsS3BuildCache();
    conf.setRegion("us-west-1");
    conf.setBucket("my-bucket");
    conf.setHeaders(null);

    BuildCacheService service = subject.createBuildCacheService(conf, buildCacheDescriber);

    assertNotNull(service);
  }


  @Test
  public void testNullHeaderName() {
    AwsS3BuildCache conf = new AwsS3BuildCache();
    conf.setRegion("us-west-1");
    conf.setBucket("my-bucket");
    Map<String, String> headers = new HashMap<String, String>(){{
      put(null, "foo");
    }};
    conf.setHeaders(headers);

    BuildCacheService service = subject.createBuildCacheService(conf, buildCacheDescriber);

    assertNotNull(service);
  }

  @Test
  public void testNullHeaderValue() {
    AwsS3BuildCache conf = new AwsS3BuildCache();
    conf.setRegion("us-west-1");
    conf.setBucket("my-bucket");
    Map<String, String> headers = new HashMap<String, String>(){{
      put("x-foo", null);
    }};
    conf.setHeaders(headers);

    BuildCacheService service = subject.createBuildCacheService(conf, buildCacheDescriber);

    assertNotNull(service);
  }

  @Test(expected = IllegalStateException.class)
  public void testIllegalConfigWithoutRegion() throws Exception {
    AwsS3BuildCache conf = new AwsS3BuildCache();
    conf.setBucket("my-bucket");

    subject.createBuildCacheService(conf, buildCacheDescriber);
  }

  @Test(expected = IllegalStateException.class)
  public void testIllegalConfigWithoutBucket() throws Exception {
    AwsS3BuildCache conf = new AwsS3BuildCache();
    conf.setRegion("us-west-1");

    subject.createBuildCacheService(conf, buildCacheDescriber);
  }

  private class NoopBuildCacheDescriber implements Describer {

    @Override
    public Describer type(String type) {
      return this;
    }

    @Override
    public Describer config(String name, String value) {
      return this;
    }

  }

}