package com.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class AWSS3Service {

    private static final Logger logger = LoggerFactory.getLogger(AWSS3Service.class);
    private final S3Client s3Client;

    public AWSS3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public Resource getPdfResourceFromS3(String bucketName, String s3Path) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        if (s3Path == null || s3Path.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 path cannot be null or empty");
        }

        try {
            logger.debug("Attempting to retrieve file from S3 - bucket: {}, path: {}", bucketName, s3Path);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Path)
                    .build();

            ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(getObjectRequest);

            if (responseInputStream == null) {
                logger.error("Null response input stream for bucket: {}, path: {}", bucketName, s3Path);
                throw new NullPointerException("S3 returned a null response input stream");
            }

            GetObjectResponse response = responseInputStream.response();

            if (response == null) {
                throw new RuntimeException("The downloaded object is not a PDF file");
            }

            if (!"application/pdf".equalsIgnoreCase(response.contentType())) {
                throw new RuntimeException("The downloaded object is not a PDF file. Content type: "
                        + response.contentType());
            }

            logger.debug("Successfully retrieved file from S3 - Content Length: {}, Content Type: {}",
                    response.contentLength(), response.contentType());

            return new InputStreamResource(java.util.Objects.requireNonNull(responseInputStream));

        } catch (S3Exception e) {

            String errorMsg = "Error downloading PDF from S3: ";

            if (e.awsErrorDetails() != null) {
                errorMsg += e.awsErrorDetails().errorMessage();
            } else {
                errorMsg += e.getMessage();
            }

            logger.error("Error retrieving file from S3 - bucket: {}, path: {}", bucketName, s3Path, e);
            throw new RuntimeException(errorMsg, e);

        } catch (NullPointerException e) {

            logger.error("Null pointer retrieving file from S3 - bucket: {}, path: {}", bucketName, s3Path, e);
            throw e;
        } catch (RuntimeException e) {

            if ("The downloaded object is not a PDF file".equals(e.getMessage()) 
                || e.getMessage().startsWith("The downloaded object is not a PDF file")) {
                throw e;
            }
            logger.error("Unexpected error retrieving file from S3 - bucket: {}, path: {}", bucketName, s3Path, e);
            throw new RuntimeException("Error downloading PDF from S3: " + e.getMessage(), e);
        }
    }
}