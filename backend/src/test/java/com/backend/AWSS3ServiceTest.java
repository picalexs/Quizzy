package com.backend;

import com.backend.service.AWSS3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AWSS3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private AWSS3Service awss3Service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPdfResourceFromS3_success() {
        GetObjectResponse mockResponse = GetObjectResponse.builder()
                .contentLength(1234L)
                .contentType("application/pdf")
                .build();
        ResponseInputStream<GetObjectResponse> mockInputStream = mock(ResponseInputStream.class);
        when(mockInputStream.response()).thenReturn(mockResponse);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockInputStream);

        Resource resource = awss3Service.getPdfResourceFromS3("test-bucket", "test-file.pdf");

        assertNotNull(resource);
        assertTrue(resource instanceof InputStreamResource);
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
    }

    @Test
    void testGetPdfResourceFromS3_success_uppercaseContentType() {
        GetObjectResponse mockResponse = GetObjectResponse.builder()
                .contentLength(1234L)
                .contentType("APPLICATION/PDF")
                .build();
        ResponseInputStream<GetObjectResponse> mockInputStream = mock(ResponseInputStream.class);
        when(mockInputStream.response()).thenReturn(mockResponse);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockInputStream);

        Resource resource = awss3Service.getPdfResourceFromS3("test-bucket", "test-file.pdf");

        assertNotNull(resource);
        assertTrue(resource instanceof InputStreamResource);
    }

    @Test
    void testGetPdfResourceFromS3_invalidBucketName_empty() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                awss3Service.getPdfResourceFromS3("", "file.pdf"));
        assertEquals("Bucket name cannot be null or empty", ex.getMessage());
    }

    @Test
    void testGetPdfResourceFromS3_invalidBucketName_null() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                awss3Service.getPdfResourceFromS3(null, "file.pdf"));
        assertEquals("Bucket name cannot be null or empty", ex.getMessage());
    }

    @Test
    void testGetPdfResourceFromS3_invalidS3Path_empty() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                awss3Service.getPdfResourceFromS3("bucket", ""));
        assertEquals("S3 path cannot be null or empty", ex.getMessage());
    }

    @Test
    void testGetPdfResourceFromS3_invalidS3Path_null() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                awss3Service.getPdfResourceFromS3("bucket", null));
        assertEquals("S3 path cannot be null or empty", ex.getMessage());
    }

    @Test
    void testGetPdfResourceFromS3_nonPdfContentType() {
        GetObjectResponse mockResponse = GetObjectResponse.builder()
                .contentLength(1234L)
                .contentType("text/plain")
                .build();
        ResponseInputStream<GetObjectResponse> mockInputStream = mock(ResponseInputStream.class);
        when(mockInputStream.response()).thenReturn(mockResponse);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockInputStream);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                awss3Service.getPdfResourceFromS3("bucket", "file.txt"));

        assertTrue(ex.getMessage().contains("The downloaded object is not a PDF file"));
    }

    @Test
    void testGetPdfResourceFromS3_s3ExceptionWithDetails() {
        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .message("Access Denied")
                .statusCode(403)
                .build();
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(s3Exception);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                awss3Service.getPdfResourceFromS3("bucket", "file.pdf"));

        assertTrue(ex.getMessage().contains("Error downloading PDF from S3"));
        assertEquals(s3Exception, ex.getCause());
    }

    @Test
    void testGetPdfResourceFromS3_s3ExceptionWithoutDetails() {
        S3Exception s3Exception = mock(S3Exception.class);
        when(s3Exception.awsErrorDetails()).thenReturn(null);
        when(s3Exception.getMessage()).thenReturn("Generic S3 error");
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(s3Exception);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                awss3Service.getPdfResourceFromS3("bucket", "file.pdf"));

        assertEquals("Error downloading PDF from S3: Generic S3 error", ex.getMessage());
        assertEquals(s3Exception, ex.getCause());
    }

    @Test
    void testGetPdfResourceFromS3_runtimeException() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                awss3Service.getPdfResourceFromS3("bucket", "file.pdf"));

        assertEquals("Error downloading PDF from S3: Unexpected error", ex.getMessage());
    }

    @Test
    void testGetPdfResourceFromS3_nullResponseInputStream() {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(null);

        assertThrows(NullPointerException.class, () ->
                awss3Service.getPdfResourceFromS3("bucket", "file.pdf"));
    }

    @Test
    void testGetPdfResourceFromS3_nullGetObjectResponse() {
        ResponseInputStream<GetObjectResponse> mockInputStream = mock(ResponseInputStream.class);
        when(mockInputStream.response()).thenReturn(null);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockInputStream);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                awss3Service.getPdfResourceFromS3("bucket", "file.pdf"));

        assertTrue(ex.getMessage().contains("The downloaded object is not a PDF file"));
    }

    @Test
    void testGetPdfResourceFromS3_nullContentType() {
        GetObjectResponse mockResponse = GetObjectResponse.builder()
                .contentLength(1234L)
                .build(); // No contentType
        ResponseInputStream<GetObjectResponse> mockInputStream = mock(ResponseInputStream.class);
        when(mockInputStream.response()).thenReturn(mockResponse);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockInputStream);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                awss3Service.getPdfResourceFromS3("bucket", "file.pdf"));

        assertTrue(ex.getMessage().contains("The downloaded object is not a PDF file"));
    }

    @Test
    void testGetPdfResourceFromS3_responseIsNull() {
        ResponseInputStream<GetObjectResponse> mockStream = mock(ResponseInputStream.class);
        when(mockStream.response()).thenReturn(null);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockStream);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                awss3Service.getPdfResourceFromS3("bucket", "file.pdf"));

        assertEquals("The downloaded object is not a PDF file", ex.getMessage());
    }

    @Test
    void testGetPdfResourceFromS3_s3ExceptionWithAwsErrorDetails() {
        S3Exception s3Exception = mock(S3Exception.class);
        software.amazon.awssdk.awscore.exception.AwsErrorDetails errorDetails = mock(software.amazon.awssdk.awscore.exception.AwsErrorDetails.class);

        when(errorDetails.errorMessage()).thenReturn("Access Denied");
        when(s3Exception.awsErrorDetails()).thenReturn(errorDetails);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(s3Exception);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                awss3Service.getPdfResourceFromS3("bucket", "file.pdf"));

        assertTrue(ex.getMessage().contains("Error downloading PDF from S3: Access Denied"));
    }


}
