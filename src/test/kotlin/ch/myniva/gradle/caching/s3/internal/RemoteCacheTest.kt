package ch.myniva.gradle.caching.s3.internal

import com.adobe.testing.s3mock.junit5.S3MockExtension
import com.amazonaws.services.s3.AmazonS3
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(S3MockExtension::class)
class RemoteCacheTest: BaseGradleTest() {
    companion object {
        const val BUCKET_NAME = "test-bucket"
    }

    @Test
    fun cacheStoreWorks(s3Client: AmazonS3) {
        s3Client.createBucket(BUCKET_NAME)
        println(s3Client.doesBucketExistV2(BUCKET_NAME))
    }
}
