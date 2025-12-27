package com.file_storage.infrastructure.storage;

import com.file_storage.application.port.out.FileStoragePort;
import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

@Component
@Slf4j
public class MinioStorageAdapter implements FileStoragePort {
    
    private final MinioClient minioClient;
    private final String bucketName;

    public MinioStorageAdapter(MinioClient minioClient, com.file_storage.infrastructure.config.MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.bucketName = minioConfig.getBucketName();
    }

    @PostConstruct
    private void initializeBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' created successfully", bucketName);
            }
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket", e);
            throw new RuntimeException("Failed to initialize MinIO bucket", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType, long size, String userId) {
        String objectName = generateObjectName(UUID.fromString(userId), fileName);
        
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("File uploaded successfully: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Error uploading file to MinIO", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error downloading file from MinIO", e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("File deleted successfully: {}", objectName);
        } catch (Exception e) {
            log.error("Error deleting file from MinIO", e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    @Override
    public String getPresignedUrl(String objectName, int expirySeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expirySeconds)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating presigned URL", e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    private String generateObjectName(UUID userId, String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString();
        String extension = "";
        
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        return String.format("%s/%s_%s%s", userId, timestamp, uuid, extension);
    }
}
