package com.file_storage.application.port.out.storage;

import lombok.Builder;

/**
 * Result object returned after successful file storage
 *
 * @param absolutePath Full path in storage (to be encrypted)
 * @param etag         Entity tag for S3/MinIO
 * @param region       For S3
 */
@Builder
public record StorageResult(String absolutePath, String bucket, String etag, long uploadedBytes, String region) {
    public StorageResult {
        if (absolutePath == null || absolutePath.isBlank()) {
            throw new IllegalArgumentException("Absolute path cannot be null or empty");
        }
        if (uploadedBytes <= 0) {
            throw new IllegalArgumentException("Uploaded bytes must be greater than 0");
        }

    }
}
