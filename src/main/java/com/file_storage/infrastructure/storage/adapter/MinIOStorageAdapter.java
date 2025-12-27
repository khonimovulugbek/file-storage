package com.file_storage.infrastructure.storage.adapter;

import com.file_storage.application.port.out.storage.FileStoragePort;
import com.file_storage.application.port.out.storage.StorageContext;
import com.file_storage.application.port.out.storage.StorageResult;
import com.file_storage.domain.model.storage.StorageReference;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO storage adapter implementing FileStoragePort
 * Handles file operations with MinIO object storage
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MinIOStorageAdapter implements FileStoragePort {
    
    private final MinioClient minioClient;
    
    @Override
    public StorageResult store(InputStream content, StorageContext context) {
        try {
            String bucket = context.bucket();
            String objectKey = buildObjectKey(context);
            
            // Ensure bucket exists
            ensureBucketExists(bucket);
            
            // Upload file
            PutObjectArgs putArgs = PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .stream(content, context.fileSize(), -1)
                .contentType(context.contentType())
                .build();
            
            ObjectWriteResponse response = minioClient.putObject(putArgs);
            
            log.info("File uploaded to MinIO: {}/{}", bucket, objectKey);
            
            return StorageResult.builder()
                .absolutePath(bucket + "/" + objectKey)
                .bucket(bucket)
                .etag(response.etag())
                .uploadedBytes(context.fileSize())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("MinIO upload failed", e);
        }
    }
    
    @Override
    public InputStream retrieve(StorageReference reference, String decryptedPath) {
        try {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String objectKey = parts[1];
            
            GetObjectArgs getArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .build();
            
            log.info("Retrieving file from MinIO: {}/{}", bucket, objectKey);
            
            return minioClient.getObject(getArgs);
            
        } catch (Exception e) {
            log.error("Failed to retrieve file from MinIO: {}", decryptedPath, e);
            throw new RuntimeException("MinIO retrieval failed", e);
        }
    }
    
    @Override
    public void delete(StorageReference reference, String decryptedPath) {
        try {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String objectKey = parts[1];
            
            RemoveObjectArgs removeArgs = RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .build();
            
            minioClient.removeObject(removeArgs);
            
            log.info("File deleted from MinIO: {}/{}", bucket, objectKey);
            
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", decryptedPath, e);
            throw new RuntimeException("MinIO deletion failed", e);
        }
    }
    
    @Override
    public boolean exists(StorageReference reference, String decryptedPath) {
        try {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String objectKey = parts[1];
            
            StatObjectArgs statArgs = StatObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .build();
            
            minioClient.statObject(statArgs);
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String generatePresignedUrl(StorageReference reference, String decryptedPath, int expirationSeconds) {
        try {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String objectKey = parts[1];
            
            GetPresignedObjectUrlArgs presignedArgs = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .object(objectKey)
                .expiry(expirationSeconds, TimeUnit.SECONDS)
                .build();
            
            String url = minioClient.getPresignedObjectUrl(presignedArgs);
            
            log.info("Generated presigned URL for MinIO object: {}/{}", bucket, objectKey);
            
            return url;
            
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for MinIO: {}", decryptedPath, e);
            throw new RuntimeException("MinIO presigned URL generation failed", e);
        }
    }
    
    private void ensureBucketExists(String bucket) throws Exception {
        boolean exists = minioClient.bucketExists(
            BucketExistsArgs.builder().bucket(bucket).build()
        );
        
        if (!exists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucket).build()
            );
            log.info("Created MinIO bucket: {}", bucket);
        }
    }
    
    private String buildObjectKey(StorageContext context) {
        // Build object key: basePath/filename
        String basePath = context.basePath() != null ? context.basePath() : "";
        return basePath.isEmpty() 
            ? context.fileName()
            : basePath + "/" + context.fileName();
    }
}
