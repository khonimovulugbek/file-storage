package com.file_storage.domain.model.storage;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing file metadata
 * Part of FileAggregate
 */
@Getter
@Builder
public class FileMetadata {
    private final String fileName;
    private final String contentType;
    private final Long fileSize;
    private final UUID ownerId;
    private final LocalDateTime uploadedAt;
    private final LocalDateTime updatedAt;
    private final FileStatus status;

    public FileMetadata(String fileName, String contentType, Long fileSize,
                       UUID ownerId, LocalDateTime uploadedAt, LocalDateTime updatedAt,
                       FileStatus status) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type cannot be null or empty");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("File size must be greater than 0");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        if (uploadedAt == null) {
            throw new IllegalArgumentException("Upload timestamp cannot be null");
        }

        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.ownerId = ownerId;
        this.uploadedAt = uploadedAt;
        this.updatedAt = updatedAt != null ? updatedAt : uploadedAt;
        this.status = status != null ? status : FileStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == FileStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return status == FileStatus.DELETED;
    }

    public enum FileStatus {
        ACTIVE,
        DELETED,
        QUARANTINED
    }
}
