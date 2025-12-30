package com.file_storage.domain.event;

import com.file_storage.domain.model.storage.FileId;

import java.time.LocalDateTime;

public record FileDownloadedEvent(
        FileId fileId,
        String fileName,
        LocalDateTime occurredOn
) implements DomainEvent {
    public static FileDownloadedEvent of(FileId fileId, String fileName) {
        return new FileDownloadedEvent(fileId, fileName, LocalDateTime.now());
    }
}
