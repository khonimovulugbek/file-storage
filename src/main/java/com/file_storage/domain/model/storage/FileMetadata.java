package com.file_storage.domain.model.storage;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FileMetadata(
        String fileName,
        String contentType,
        Long fileSize,
        FileStatusEnum status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {


    public boolean isActive() {
        return status == FileStatusEnum.ACTIVE;
    }

    public enum FileStatusEnum {
        ACTIVE,
        INACTIVE,
        DELETED
    }
}
