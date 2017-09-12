package ch.myniva.gradle.caching.s3.internal;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.StorageClass;
import org.gradle.caching.BuildCacheEntryWriter;
import org.gradle.caching.BuildCacheKey;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AwsS3BuildCacheServiceTest {
    @Mock
    AmazonS3 s3;
    @Mock
    BuildCacheKey key;
    @Mock
    BuildCacheEntryWriter writer;
    @Mock
    PutObjectRequest putObjectRequest = mock(PutObjectRequest.class);

    AwsS3BuildCacheService buildCacheService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void storePutsObjectAndUsesReducedRedundancyWhenConfigured() throws IOException {
        /** Setup **/
        buildCacheService = spy(new AwsS3BuildCacheService(s3, "bucketName", true));
        doReturn(putObjectRequest).when(buildCacheService).getPutObjectRequest(any(BuildCacheKey.class),
                any(ObjectMetadata.class), any(InputStream.class));

        /** Run **/
        buildCacheService.store(key, writer);

        /** Check **/
        verifyThatStoreStores();
        verify(putObjectRequest).withStorageClass(eq(StorageClass.ReducedRedundancy));
    }

    @Test
    public void storePutsObjectAndDoesNotUseReducedRedundancyWhenConfigured() throws IOException {
        /** Setup **/
        buildCacheService = spy(new AwsS3BuildCacheService(s3, "bucketName", false));
        doReturn(putObjectRequest).when(buildCacheService).getPutObjectRequest(any(BuildCacheKey.class),
                any(ObjectMetadata.class), any(InputStream.class));

        /** Run **/
        buildCacheService.store(key, writer);

        /** Check **/
        verifyThatStoreStores();
        verify(putObjectRequest, never()).withStorageClass(eq(StorageClass.ReducedRedundancy));
    }

    private void verifyThatStoreStores() throws IOException {
        verify(writer).writeTo(any(ByteArrayOutputStream.class));
        verify(buildCacheService).getPutObjectRequest(eq(key), any(ObjectMetadata.class), any(InputStream.class));
        verify(s3).putObject(eq(putObjectRequest));
    }
}
