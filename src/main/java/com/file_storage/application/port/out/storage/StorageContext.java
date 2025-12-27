package com.file_storage.application.port.out.storage;

import com.file_storage.domain.model.storage.StorageNode;
import lombok.Builder;

/**
 * Context object for file storage operations
 * Contains all necessary information for storing a file
 *
 * @param basePath Base directory for organizing files
 */
@Builder
public record StorageContext(String fileName, String contentType, long fileSize, String bucket, StorageNode targetNode,
                             String basePath) {
    public StorageContext {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type cannot be null or empty");
        }
        if (fileSize <= 0) {
            throw new IllegalArgumentException("File size must be greater than 0");
        }
        if (targetNode == null) {
            throw new IllegalArgumentException("Target node cannot be null");
        }

    }
}
