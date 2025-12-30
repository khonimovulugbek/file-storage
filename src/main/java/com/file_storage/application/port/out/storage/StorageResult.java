package com.file_storage.application.port.out.storage;

import lombok.Builder;

@Builder
public record StorageResult(
        String absolutePath,
        String path,
        String bucket,
        String etag,
        long bytes,
        String region
) {
    public StorageResult {
        if (absolutePath == null || absolutePath.isBlank()) {
            throw new IllegalArgumentException("Absolute path cannot be null or empty");
        }
        if (bytes <= 0) {
            throw new IllegalArgumentException("Uploaded bytes must be greater than 0");
        }

    }
}
