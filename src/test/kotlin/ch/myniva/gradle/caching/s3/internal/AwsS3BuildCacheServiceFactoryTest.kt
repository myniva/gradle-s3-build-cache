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
package ch.myniva.gradle.caching.s3.internal

import ch.myniva.gradle.caching.s3.AwsS3BuildCache
import org.gradle.caching.BuildCacheServiceFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AwsS3BuildCacheServiceFactoryTest {
    private lateinit var subject: AwsS3BuildCacheServiceFactory
    private lateinit var buildCacheDescriber: BuildCacheServiceFactory.Describer

    private inner class NoopBuildCacheDescriber : BuildCacheServiceFactory.Describer {
        override fun type(type: String) = this
        override fun config(name: String, value: String) = this
    }

    @BeforeEach
    fun setUp() {
        subject = AwsS3BuildCacheServiceFactory()
        buildCacheDescriber = NoopBuildCacheDescriber()
    }

    private fun buildCache(action: AwsS3BuildCache.()->Unit) = AwsS3BuildCache().apply(action)

    @Test
    fun testWhat() {
        val conf = buildCache {
            region = "us-west-1"
            bucket = "my-bucket"
        }
        val service = subject.createBuildCacheService(conf, buildCacheDescriber)
        Assertions.assertNotNull(service)
    }

    @Test
    fun testPath() {
        val conf = buildCache {
            region = "us-west-1"
            bucket = "my-bucket"
            path = "cache"
        }
        val service = subject.createBuildCacheService(conf, buildCacheDescriber)
        Assertions.assertNotNull(service)
    }

    @Test
    fun testNullHeaders() {
        val conf = buildCache {
            region = "us-west-1"
            bucket = "my-bucket"
            headers = null
        }
        val service = subject.createBuildCacheService(conf, buildCacheDescriber)
        Assertions.assertNotNull(service)
    }

    @Test
    fun testNullHeaderName() {
        val conf = buildCache {
            region = "us-west-1"
            bucket = "my-bucket"
            headers = mapOf(null to "foo")
        }
        val service = subject.createBuildCacheService(conf, buildCacheDescriber)
        Assertions.assertNotNull(service)
    }

    @Test
    fun testNullHeaderValue() {
        val conf = buildCache {
            region = "us-west-1"
            bucket = "my-bucket"
            headers = mapOf("x-foo" to null)
        }
        val service = subject.createBuildCacheService(conf, buildCacheDescriber)
        Assertions.assertNotNull(service)
    }

    @Test
    fun testIllegalConfigWithoutRegion() {
        val conf = buildCache {
            bucket = "my-bucket"
        }
        assertThrows<IllegalStateException> {
            subject.createBuildCacheService(conf, buildCacheDescriber)
        }
    }

    @Test
    fun testIllegalConfigWithoutBucket() {
        val conf = buildCache {
            region = "us-west-1"
        }
        assertThrows<IllegalStateException> {
            subject.createBuildCacheService(conf, buildCacheDescriber)
        }
    }

    @Test
    fun testAddAWSSessionCredentials() {
        val conf = buildCache {
            bucket = "my-bucket"
            region = "us-west-1"
            awsAccessKeyId = "any aws access key"
            awsSecretKey = "any secret key"
            sessionToken = "any session token"
        }
        val service = subject.createBuildCacheService(conf, buildCacheDescriber)
        Assertions.assertNotNull(service)
    }
}
