package com.file_storage.infrastructure.web.dto.response.storage;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FileUploadResponse(
        String fileId,
        String fileName,
        long fileSize,
        String contentType,
        String checksum,
        String storageNodeId,
        String absolutePath,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
