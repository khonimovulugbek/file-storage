package com.file_storage.infrastructure.storage.adapter;

import com.file_storage.application.port.out.storage.FileStoragePort;
import com.file_storage.application.port.out.storage.StorageContext;
import com.file_storage.application.port.out.storage.StorageNodeRegistryPort;
import com.file_storage.application.port.out.storage.StorageResult;
import com.file_storage.domain.model.storage.StorageNode;
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
 * Creates clients dynamically per storage node
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MinIOStorageAdapter implements FileStoragePort {
    
    private final StorageNodeRegistryPort nodeRegistry;
    
    @Override
    public StorageResult store(InputStream content, StorageContext context) {
        MinioClient client = createClient(context.targetNode());
        try {
            String bucket = context.bucket();
            String objectKey = buildObjectKey(context);
            
            // Ensure bucket exists
            ensureBucketExists(client, bucket);
            
            // Upload file
            PutObjectArgs putArgs = PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .stream(content, context.fileSize(), -1)
                .contentType(context.contentType())
                .build();
            
            ObjectWriteResponse response = client.putObject(putArgs);
            
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
        StorageNode node = getNodeById(reference.getStorageNodeId());
        MinioClient client = createClient(node);
        try {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String objectKey = parts[1];
            
            GetObjectArgs getArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .build();
            
            log.info("Retrieving file from MinIO: {}/{}", bucket, objectKey);
            
            return client.getObject(getArgs);
            
        } catch (Exception e) {
            log.error("Failed to retrieve file from MinIO: {}", decryptedPath, e);
            throw new RuntimeException("MinIO retrieval failed", e);
        }
    }
    
    @Override
    public void delete(StorageReference reference, String decryptedPath) {
        StorageNode node = getNodeById(reference.getStorageNodeId());
        MinioClient client = createClient(node);
        try {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String objectKey = parts[1];
            
            RemoveObjectArgs removeArgs = RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .build();
            
            client.removeObject(removeArgs);
            
            log.info("File deleted from MinIO: {}/{}", bucket, objectKey);
            
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", decryptedPath, e);
            throw new RuntimeException("MinIO deletion failed", e);
        }
    }
    
    @Override
    public boolean exists(StorageReference reference, String decryptedPath) {
        StorageNode node = getNodeById(reference.getStorageNodeId());
        MinioClient client = createClient(node);
        try {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String objectKey = parts[1];
            
            StatObjectArgs statArgs = StatObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .build();
            
            client.statObject(statArgs);
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String generatePresignedUrl(StorageReference reference, String decryptedPath, int expirationSeconds) {
        StorageNode node = getNodeById(reference.getStorageNodeId());
        MinioClient client = createClient(node);
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
            
            String url = client.getPresignedObjectUrl(presignedArgs);
            
            log.info("Generated presigned URL for MinIO object: {}/{}", bucket, objectKey);
            
            return url;
            
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for MinIO: {}", decryptedPath, e);
            throw new RuntimeException("MinIO presigned URL generation failed", e);
        }
    }
    
    private StorageNode getNodeById(String nodeId) {
        return nodeRegistry.findById(nodeId)
            .orElseThrow(() -> new RuntimeException("Storage node not found: " + nodeId));
    }
    
    private MinioClient createClient(StorageNode node) {
        return MinioClient.builder()
            .endpoint(node.nodeUrl())
            .credentials(node.accessKey(), node.secretKey())
            .build();
    }
    
    private void ensureBucketExists(MinioClient client, String bucket) throws Exception {
        boolean exists = client.bucketExists(
            BucketExistsArgs.builder().bucket(bucket).build()
        );
        
        if (!exists) {
            client.makeBucket(
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
