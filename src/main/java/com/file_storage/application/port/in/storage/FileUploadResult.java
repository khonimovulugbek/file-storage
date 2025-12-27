package com.file_storage.application.port.in.storage;

import com.file_storage.domain.model.storage.FileId;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Result object returned after successful file upload
 *
 * @param deduplicated True if file already existed
 */
@Builder
public record FileUploadResult(FileId fileId, String fileName, long fileSize, String contentType, String checksum,
                               String storageNodeId, LocalDateTime uploadedAt, boolean deduplicated) {
    public FileUploadResult {
        if (fileId == null) {
            throw new IllegalArgumentException("File ID cannot be null");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (uploadedAt == null) {
            throw new IllegalArgumentException("Upload timestamp cannot be null");
        }

    }
}
