package ch.myniva.gradle.caching.s3.internal;

import static org.junit.Assert.assertNotNull;

import ch.myniva.gradle.caching.s3.AwsS3BuildCache;
import org.gradle.api.GradleException;
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

  @Test(expected = GradleException.class)
  public void testIllegalConfigWithInvalidRegion() throws Exception {
    AwsS3BuildCache conf = new AwsS3BuildCache();
    conf.setRegion("not-a-valid-aws-region");
    conf.setBucket("my-bucket");

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