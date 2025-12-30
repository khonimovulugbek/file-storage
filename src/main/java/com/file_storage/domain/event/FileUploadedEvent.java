package com.file_storage.domain.event;

import com.file_storage.domain.model.storage.FileId;

import java.time.LocalDateTime;

public record FileUploadedEvent(
        FileId fileId,
        String fileName,
        long fileSize,
        String storageNodeId,
        LocalDateTime occurredOn
) implements DomainEvent {
    public static FileUploadedEvent of(FileId fileId, String fileName, long fileSize, String storageNodeId) {
        return new FileUploadedEvent(fileId, fileName, fileSize, storageNodeId, LocalDateTime.now());
    }
}
