package com.file_storage.infrastructure.storage.adapter;

import com.file_storage.application.port.out.storage.FileStoragePort;
import com.file_storage.application.port.out.storage.StorageContext;
import com.file_storage.application.port.out.storage.StorageNodeRegistryPort;
import com.file_storage.application.port.out.storage.StorageResult;
import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
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
 * Creates clients dynamically per storage node
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class S3StorageAdapter implements FileStoragePort {
    
    private final StorageNodeRegistryPort nodeRegistry;
    
    @Override
    public StorageResult store(InputStream content, StorageContext context) {
        StorageNode node = context.targetNode();
        try (S3Client client = createS3Client(node)) {
            String bucket = context.bucket();
            String key = buildKey(context);
            
            // Ensure bucket exists
            ensureBucketExists(client, bucket);
            
            // Upload file
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(context.contentType())
                .contentLength(context.fileSize())
                .build();
            
            PutObjectResponse response = client.putObject(
                putRequest,
                RequestBody.fromInputStream(content, context.fileSize())
            );
            
            log.info("File uploaded to S3: s3://{}/{}", bucket, key);
            
            return StorageResult.builder()
                .absolutePath(bucket + "/" + key)
                .bucket(bucket)
                .etag(response.eTag())
                .uploadedBytes(context.fileSize())
                .region(context.targetNode().nodeUrl())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("S3 upload failed", e);
        }
    }
    
    @Override
    public InputStream retrieve(StorageReference reference, String decryptedPath) {
        StorageNode node = getNodeById(reference.getStorageNodeId());
        try (S3Client client = createS3Client(node)) {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String key = parts[1];
            
            GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            
            log.info("Retrieving file from S3: s3://{}/{}", bucket, key);
            
            return client.getObject(getRequest);
            
        } catch (Exception e) {
            log.error("Failed to retrieve file from S3: {}", decryptedPath, e);
            throw new RuntimeException("S3 retrieval failed", e);
        }
    }
    
    @Override
    public void delete(StorageReference reference, String decryptedPath) {
        StorageNode node = getNodeById(reference.getStorageNodeId());
        try (S3Client client = createS3Client(node)) {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String key = parts[1];
            
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            
            client.deleteObject(deleteRequest);
            
            log.info("File deleted from S3: s3://{}/{}", bucket, key);
            
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", decryptedPath, e);
            throw new RuntimeException("S3 deletion failed", e);
        }
    }
    
    @Override
    public boolean exists(StorageReference reference, String decryptedPath) {
        StorageNode node = getNodeById(reference.getStorageNodeId());
        try (S3Client client = createS3Client(node)) {
            String[] parts = decryptedPath.split("/", 2);
            String bucket = parts[0];
            String key = parts[1];
            
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            
            client.headObject(headRequest);
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
        StorageNode node = getNodeById(reference.getStorageNodeId());
        try (S3Presigner presigner = createS3Presigner(node)) {
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
            
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            
            log.info("Generated presigned URL for S3 object: s3://{}/{}", bucket, key);
            
            return presignedRequest.url().toString();
            
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for S3: {}", decryptedPath, e);
            throw new RuntimeException("S3 presigned URL generation failed", e);
        }
    }
    
    private StorageNode getNodeById(String nodeId) {
        return nodeRegistry.findById(nodeId)
            .orElseThrow(() -> new RuntimeException("Storage node not found: " + nodeId));
    }
    
    private S3Client createS3Client(StorageNode node) {
        String region = extractRegion(node.nodeUrl());
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(node.accessKey(), node.secretKey())
            ))
            .build();
    }
    
    private S3Presigner createS3Presigner(StorageNode node) {
        String region = extractRegion(node.nodeUrl());
        return S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(node.accessKey(), node.secretKey())
            ))
            .build();
    }
    
    private String extractRegion(String nodeUrl) {
        // Extract region from S3 URL or default to us-east-1
        // Format: https://s3.{region}.amazonaws.com or https://s3-{region}.amazonaws.com
        if (nodeUrl.contains(".amazonaws.com")) {
            String[] parts = nodeUrl.split("\\.");
            if (parts.length >= 3) {
                String regionPart = parts[1];
                if (regionPart.startsWith("s3-")) {
                    return regionPart.substring(3);
                } else if (!regionPart.equals("s3")) {
                    return regionPart;
                }
            }
        }
        return "us-east-1";
    }
    
    private void ensureBucketExists(S3Client client, String bucket) {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            CreateBucketRequest createRequest = CreateBucketRequest.builder()
                .bucket(bucket)
                .build();
            client.createBucket(createRequest);
            log.info("Created S3 bucket: {}", bucket);
        }
    }
    
    private String buildKey(StorageContext context) {
        String basePath = context.basePath() != null ? context.basePath() : "";
        return basePath.isEmpty() 
            ? context.fileName()
            : basePath + "/" + context.fileName();
    }
}
