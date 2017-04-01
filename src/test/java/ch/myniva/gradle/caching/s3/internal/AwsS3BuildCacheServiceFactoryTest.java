package ch.myniva.gradle.caching.s3.internal;

import static org.junit.Assert.assertNotNull;

import ch.myniva.gradle.caching.s3.AwsS3BuildCache;
import org.gradle.api.GradleException;
import org.gradle.caching.BuildCacheService;
import org.junit.Before;
import org.junit.Test;

public class AwsS3BuildCacheServiceFactoryTest {

  private AwsS3BuildCacheServiceFactory subject;

  @Before
  public void setUp() {
    subject = new AwsS3BuildCacheServiceFactory();
  }

  @Test
  public void testWhat() {
    AwsS3BuildCache conf = new AwsS3BuildCache();
    conf.setRegion("us-west-1");
    conf.setBucket("my-bucket");

    BuildCacheService service = subject.createBuildCacheService(conf);

    assertNotNull(service);
  }

  @Test(expected = IllegalStateException.class)
  public void testIllegalConfigWithoutRegion() throws Exception {
    AwsS3BuildCache conf = new AwsS3BuildCache();
    conf.setBucket("my-bucket");

    subject.createBuildCacheService(conf);
  }

  @Test(expected = IllegalStateException.class)
  public void testIllegalConfigWithoutBucket() throws Exception {
    AwsS3BuildCache conf = new AwsS3BuildCache();
    conf.setRegion("us-west-1");

    subject.createBuildCacheService(conf);
  }

  @Test(expected = GradleException.class)
  public void testIllegalConfigWithInvalidRegion() throws Exception {
    AwsS3BuildCache conf = new AwsS3BuildCache();
    conf.setRegion("not-a-valid-aws-region");
    conf.setBucket("my-bucket");

    subject.createBuildCacheService(conf);
  }

}