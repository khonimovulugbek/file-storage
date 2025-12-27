package com.file_storage.domain.model.storage;

import lombok.Builder;
import lombok.Getter;

/**
 * Aggregate Root for File domain
 * Encapsulates all file-related data and business rules
 */
@Getter
@Builder
public class FileAggregate {
    private final FileId fileId;
    private final FileMetadata metadata;
    private final StorageReference storageReference;
    private final FileChecksum checksum;

    public FileAggregate(FileId fileId, FileMetadata metadata, 
                        StorageReference storageReference, FileChecksum checksum) {
        if (fileId == null) {
            throw new IllegalArgumentException("File ID cannot be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("File metadata cannot be null");
        }
        if (storageReference == null) {
            throw new IllegalArgumentException("Storage reference cannot be null");
        }
        if (checksum == null) {
            throw new IllegalArgumentException("File checksum cannot be null");
        }

        this.fileId = fileId;
        this.metadata = metadata;
        this.storageReference = storageReference;
        this.checksum = checksum;
    }

    /**
     * Domain rule: File can only be downloaded if active
     */
    public boolean canBeDownloaded() {
        return metadata.isActive();
    }

    /**
     * Domain rule: Check if user owns this file
     */
    public boolean isOwnedBy(java.util.UUID userId) {
        return metadata.getOwnerId().equals(userId);
    }

    /**
     * Domain rule: Verify file integrity
     */
    public boolean verifyChecksum(FileChecksum providedChecksum) {
        return this.checksum.equals(providedChecksum);
    }

    /**
     * Get storage type for routing
     */
    public StorageType getStorageType() {
        return storageReference.getStorageType();
    }

    /**
     * Get storage node ID for resolution
     */
    public String getStorageNodeId() {
        return storageReference.getStorageNodeId();
    }
}
