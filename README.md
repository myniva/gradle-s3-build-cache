# AWS S3 Gradle build cache

[![Apache License 2.0](https://img.shields.io/badge/License-Apache%20License%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![GitHub version](https://badge.fury.io/gh/myniva%2Fgradle-s3-build-cache.svg)](https://badge.fury.io/gh/myniva%2Fgradle-s3-build-cache)
[![Build status](https://api.travis-ci.org/myniva/gradle-s3-build-cache.svg?branch=develop)](https://travis-ci.org/myniva/gradle-s3-build-cache)

This is a custom Gradle [build cache](https://docs.gradle.org/current/userguide/build_cache.html)
implementation which uses [AWS S3](https://aws.amazon.com/s3/) to store the cache objects.


## Compatibility

* Version >= 0.3.0 - Gradle 4.x
* Version < 0.3.0 - Gradle 3.5

## Use in your project

Please note that this plugin is not yet ready for production. Feedback though is very welcome.
Please open an [issue](https://github.com/myniva/gradle-s3-build-cache/issues) if you find a bug or 
have an idea for an improvement.


### Apply plugin

The Gradle build cache needs to be configured on the Settings level. As a first step, add a
dependency to the plugin to your `settings.gradle` file:

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.ch.myniva.gradle:s3-build-cache:0.1.0"
  }
}
```

### Configuration

The AWS S3 build cache implementation has a few configuration options:

| Configuration Key | Description | Default Value |
| ----------------- | ----------- | ----------- |
| region | The AWS region the S3 bucket is located in. | |
| bucket | The name of the AWS S3 bucket where cache objects should be stored. | |
| reducedRedundancy | Whether or not to use [reduced redundancy](https://aws.amazon.com/s3/reduced-redundancy/). | true |

(Options without a default value are mandatory.)


The `buildCache` configuration block might look like this:

```
 apply plugin: 'ch.myniva.s3-build-cache'
 
 ext.isCiServer = System.getenv().containsKey("CI")
 
 buildCache {
     local {
         enabled = !isCiServer
     }
     remote(ch.myniva.gradle.caching.s3.AwsS3BuildCache) {
         region = 'eu-west-1'
         bucket = 'your-bucket'
         push = isCiServer
     }
 }

```

More details about configuring the Gradle build cache can be found in the
[official Gradle documentation](https://docs.gradle.org/current/userguide/build_cache.html#sec:build_cache_configure).


### AWS credentials

The plugin uses the [`DefaultAWSCredentialsProviderChain`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html)
to look up the AWS credentials.


### Run build with build cache

The Gradle build cache is an incubating feature and needs to be enabled per build (`--build-cache`)
or in the Gradle properties (`org.gradle.caching=true`).

Example:

```
$> gradle --build-cache assemble
Build cache is an incubating feature.
Using AWS S3 Build Cache (your-bucket) as remote build cache, push is enabled.
```


## Contributing

Contributions are always welcome! If you'd like to contribute (and we hope you do) please open a pull request.


## License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
