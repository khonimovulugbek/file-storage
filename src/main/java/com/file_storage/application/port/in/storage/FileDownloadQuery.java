package com.file_storage.application.port.in.storage;

import com.file_storage.domain.model.storage.FileId;
import lombok.Builder;

import java.util.UUID;

/**
 * Query object for file download
 * Encapsulates file ID and user ID for authorization
 */
@Builder
public record FileDownloadQuery(FileId fileId, UUID userId) {
    public FileDownloadQuery {
        if (fileId == null) {
            throw new IllegalArgumentException("File ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

    }
}
