package com.file_storage.domain.model.storage;

import lombok.Builder;
import com.file_storage.domain.exception.InvalidFileException;

@Builder
public record FileAggregate(
        FileId fileId,
        FileMetadata fileMetadata,
        StorageReference storageReference,
        FileChecksum checksum
) {

    public FileAggregate {
        if (fileId == null) {
            throw new InvalidFileException("FileId cannot be null");
        }
        if (fileMetadata == null) {
            throw new InvalidFileException("FileMetadata cannot be null");
        }
        if (storageReference == null) {
            throw new InvalidFileException("StorageReference cannot be null");
        }
        if (checksum == null) {
            throw new InvalidFileException("FileChecksum cannot be null");
        }
    }

    public boolean canBeDownloaded() {
        return fileMetadata.isActive();
    }

    public StorageType storageType() {
        return storageReference.storageType();
    }

    public String storageNodeId() {
        return storageReference.storageNodeId();
    }

    public void validateForDownload() {
        if (!canBeDownloaded()) {
            throw new InvalidFileException("File is not available for download: " + fileId.value());
        }
    }
}
