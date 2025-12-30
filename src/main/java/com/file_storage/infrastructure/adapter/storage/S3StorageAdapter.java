package com.file_storage.infrastructure.adapter.storage;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import com.file_storage.application.port.out.storage.FileStoragePort;
import com.file_storage.application.port.out.storage.StorageContext;
import com.file_storage.application.port.out.storage.StorageResult;
import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageReference;
import com.file_storage.infrastructure.annotation.Adapter;

import java.io.InputStream;

@Adapter
@Slf4j
public class S3StorageAdapter implements FileStoragePort {
    @Override
    public StorageResult store(InputStream content, StorageContext context) {
        StorageNode node = context.storageNode();
        String region = extractRegion(node.nodeUrl());

        try (S3Client client = createS3Client(node, region)) {
            String bucket = context.bucket();
            String objectKey = buildKey(context);
            ensureBucketExists(client, bucket);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(context.contentType())
                    .contentLength(context.fileSize())
                    .build();

            PutObjectResponse response = client.putObject(
                    request, RequestBody.fromInputStream(content, context.fileSize())
            );
            return StorageResult.builder()
                    .absolutePath(node.publicNodeUrl() + "/" + bucket + "/" + objectKey)
                    .path(bucket + "/" + objectKey)
                    .bucket(bucket)
                    .etag(response.eTag())
                    .bytes(context.fileSize())
                    .region(region)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("S3 upload failed", e);
        }

    }

    @Override
    public InputStream retrieve(StorageReference storageReference) {
        return null;
    }

    private void ensureBucketExists(S3Client client, String bucket) {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (Exception e) {
            CreateBucketRequest request = CreateBucketRequest.builder().bucket(bucket).build();
            client.createBucket(request);
        }
    }

    private String buildKey(StorageContext context) {
        String basePath = context.basePath() == null ? "" : context.basePath();
        return basePath.isBlank()
                ? context.fileName()
                : basePath + "/" + context.fileName();
    }

    private S3Client createS3Client(StorageNode node, String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(node.accessKey(), node.secretKey())
                ))
                .build();
    }

    private String extractRegion(String nodeUrl) {
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
}
