package com.file_storage.infrastructure.storage.adapter;

import com.file_storage.application.port.out.storage.FileStoragePort;
import com.file_storage.application.port.out.storage.StorageContext;
import com.file_storage.application.port.out.storage.StorageResult;
import com.file_storage.domain.model.storage.StorageReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;

/**
 * AWS S3 storage adapter implementing FileStoragePort
 * Handles file operations with AWS S3
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class S3StorageAdapter implements FileStoragePort {
    
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    
    @Value("${aws.region:us-east-1}")
    private String awsRegion;
    
    @Override
    public StorageResult store(InputStream content, StorageContext context) {
        try {
            String bucket = context.bucket();
            String key = buildKey(context);
            
            // Ensure bucket exists
            ensureBucketExists(bucket);
            
            // Upload file
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(context.contentType())
                .contentLength(context.fileSize())
                .build();
            
            PutObjectResponse response = s3Client.putObject(
                putRequest,
                RequestBody.fromInputStream(content, context.fileSize())
            );
            
            log.info("File uploaded to S3: s3://{}/{}", bucket, key);
            
            return StorageResult.builder()
                .absolutePath(bucket + "/" + key)
                .bucket(bucket)
                .etag(response.eTag())
                .uploadedBytes(context.fileSize())
                .region(getRegion())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("S3 upload failed", e);
        }
    }
    
    @Override
    public InputStream retrieve(StorageReference reference, String decryptedPath) {
        try {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String key = parts[1];
            
            GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            
            log.info("Retrieving file from S3: s3://{}/{}", bucket, key);
            
            return s3Client.getObject(getRequest);
            
        } catch (Exception e) {
            log.error("Failed to retrieve file from S3: {}", decryptedPath, e);
            throw new RuntimeException("S3 retrieval failed", e);
        }
    }
    
    @Override
    public void delete(StorageReference reference, String decryptedPath) {
        try {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String key = parts[1];
            
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            
            s3Client.deleteObject(deleteRequest);
            
            log.info("File deleted from S3: s3://{}/{}", bucket, key);
            
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", decryptedPath, e);
            throw new RuntimeException("S3 deletion failed", e);
        }
    }
    
    @Override
    public boolean exists(StorageReference reference, String decryptedPath) {
        try {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String key = parts[1];
            
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            
            s3Client.headObject(headRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking S3 object existence: {}", decryptedPath, e);
            return false;
        }
    }
    
    @Override
    public String generatePresignedUrl(StorageReference reference, String decryptedPath, int expirationSeconds) {
        try {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String key = parts[1];
            
            GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expirationSeconds))
                .getObjectRequest(getRequest)
                .build();
            
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            
            log.info("Generated presigned URL for S3 object: s3://{}/{}", bucket, key);
            
            return presignedRequest.url().toString();
            
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for S3: {}", decryptedPath, e);
            throw new RuntimeException("S3 presigned URL generation failed", e);
        }
    }
    
    private void ensureBucketExists(String bucket) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            CreateBucketRequest createRequest = CreateBucketRequest.builder()
                .bucket(bucket)
                .build();
            s3Client.createBucket(createRequest);
            log.info("Created S3 bucket: {}", bucket);
        }
    }
    
    private String buildKey(StorageContext context) {
        String basePath = context.basePath() != null ? context.basePath() : "";
        return basePath.isEmpty() 
            ? context.fileName()
            : basePath + "/" + context.fileName();
    }
    
    private String getRegion() {
        return awsRegion;
    }
}
