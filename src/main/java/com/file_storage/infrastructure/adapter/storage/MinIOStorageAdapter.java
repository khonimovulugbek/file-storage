package com.file_storage.infrastructure.adapter.storage;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import com.file_storage.application.port.out.storage.FileStoragePort;
import com.file_storage.application.port.out.storage.StorageContext;
import com.file_storage.application.port.out.storage.StorageNodeRegistryPort;
import com.file_storage.application.port.out.storage.StorageResult;
import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageReference;
import com.file_storage.infrastructure.annotation.Adapter;

import java.io.InputStream;

@Adapter
@Slf4j
public class MinIOStorageAdapter implements FileStoragePort {
    private final StorageNodeRegistryPort nodeRegistry;

    public MinIOStorageAdapter(StorageNodeRegistryPort nodeRegistry) {
        this.nodeRegistry = nodeRegistry;
    }

    @Override
    public StorageResult store(InputStream content, StorageContext context) {
        StorageNode storageNode = context.storageNode();
        MinioClient client = createClient(storageNode);
        try {
            String bucket = context.bucket();
            String objectKey = buildObjectKey(context);
            ensureBucketExists(client, bucket);

            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(content, content.available(), -1)
                    .contentType(context.contentType())
                    .build();
            ObjectWriteResponse response = client.putObject(args);
            return StorageResult.builder()
                    .absolutePath(storageNode.publicNodeUrl() + "/" + bucket + "/" + objectKey)
                    .path(bucket + "/" + objectKey)
                    .bucket(bucket)
                    .etag(response.etag())
                    .bytes(context.fileSize())
                    .build();
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("MinIO upload failed", e);
        }
    }

    @Override
    public InputStream retrieve(StorageReference storageReference) {
        StorageNode node = getStorageNode(storageReference.storageNodeId());
        MinioClient client = createClient(node);
        String path = storageReference.path();
        try {
            String[] parts = path.split("/", 2);
            String bucket = parts[0];
            String objectKey = parts[1];
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build();
            return client.getObject(args);
        } catch (Exception e) {
            log.error("Failed to retrieve file from MinIO: {}", path, e);
            throw new RuntimeException("MinIO retrieval failed", e);
        }
    }

    private StorageNode getStorageNode(String nodeId) {
        return nodeRegistry.findById(nodeId);
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
            setBucketPolicy(client, bucket);
        }
    }

    private void setBucketPolicy(MinioClient client, String bucketName) {
        try {
            String minioAllowPolicy = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {
                                    "AWS": [
                                        "*"
                                    ]
                                },
                                "Action": [
                                    "s3:GetBucketLocation",
                                    "s3:ListBucket"
                                ],
                                "Resource": [
                                    "arn:aws:s3:::*"
                                ]
                            },
                            {
                                "Effect": "Allow",
                                "Principal": {
                                    "AWS": [
                                        "*"
                                    ]
                                },
                                "Action": [
                                    "s3:GetObject"
                                ],
                                "Resource": [
                                    "arn:aws:s3:::*"
                                ]
                            }
                        ]
                    }
                    """;
            client.setBucketPolicy(
                    SetBucketPolicyArgs.builder().bucket(bucketName)
                            .config(minioAllowPolicy)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());

        }
    }

    private String buildObjectKey(StorageContext context) {
        String basePath = context.basePath() == null ? "" : context.basePath();
        return basePath.isBlank()
                ? context.fileName()
                : basePath + "/" + context.fileName();
    }
}
