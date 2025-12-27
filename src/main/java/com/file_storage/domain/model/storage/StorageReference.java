package com.file_storage.domain.model.storage;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Value Object representing encrypted storage location
 * CRITICAL: Path is ALWAYS encrypted - never store raw paths
 */
@Getter
@EqualsAndHashCode
@Builder
public class StorageReference {
    private final StorageType storageType;
    private final String storageNodeId;
    private final String encryptedPath;  // AES-256 encrypted absolute path
    private final String encryptionKeyRef;  // Reference to key vault
    private final String bucket;  // For S3/MinIO
    private final String region;  // For S3

    public StorageReference(StorageType storageType, String storageNodeId, 
                           String encryptedPath, String encryptionKeyRef,
                           String bucket, String region) {
        if (storageType == null) {
            throw new IllegalArgumentException("Storage type cannot be null");
        }
        if (storageNodeId == null || storageNodeId.isBlank()) {
            throw new IllegalArgumentException("Storage node ID cannot be null or empty");
        }
        if (encryptedPath == null || encryptedPath.isBlank()) {
            throw new IllegalArgumentException("Encrypted path cannot be null or empty");
        }
        if (encryptionKeyRef == null || encryptionKeyRef.isBlank()) {
            throw new IllegalArgumentException("Encryption key reference cannot be null or empty");
        }

        this.storageType = storageType;
        this.storageNodeId = storageNodeId;
        this.encryptedPath = encryptedPath;
        this.encryptionKeyRef = encryptionKeyRef;
        this.bucket = bucket;
        this.region = region;
    }

    public boolean requiresBucket() {
        return storageType == StorageType.MINIO || storageType == StorageType.S3;
    }

    public boolean requiresRegion() {
        return storageType == StorageType.S3;
    }
}
