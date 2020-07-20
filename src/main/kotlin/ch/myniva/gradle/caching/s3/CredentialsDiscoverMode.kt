package ch.myniva.gradle.caching.s3

enum class CredentialsDiscoverMode {
    /**
     * Use [DefaultAWSCredentialsProviderChain]
     */
    AWS_DEFAULT,

    /**
     * Use [DefaultAWSCredentialsProviderChain], however skip EC2 metadata request.
     * This is helpful for local development as developer machines typically do not run EC2
     * metadata services.
     */
    AWS_LOCAL_ONLY,

    /**
     * Avoids AWS credentials lookup, and use anonymous access if no credentials provided
     */
    ANONYMOUS_IF_MISSING
}
