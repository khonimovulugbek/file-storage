package com.file_storage.application.port.in.storage;

import lombok.Builder;

import java.io.InputStream;
import java.util.UUID;

/**
 * Command object for file upload
 * Encapsulates all data needed for upload operation
 *
 * @param preferredStorageType Optional: MINIO, S3, SFTP
 */
@Builder
public record FileUploadCommand(InputStream fileContent, String fileName, String contentType, long fileSize,
                                UUID ownerId, String preferredStorageType) {
    public FileUploadCommand {
        if (fileContent == null) {
            throw new IllegalArgumentException("File content cannot be null");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type cannot be null or empty");
        }
        if (fileSize <= 0) {
            throw new IllegalArgumentException("File size must be greater than 0");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }

    }
}
