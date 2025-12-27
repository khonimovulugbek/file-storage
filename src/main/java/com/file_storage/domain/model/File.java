package com.file_storage.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File {
    private UUID id;
    private String name;
    private Long size;
    private String contentType;
    private String checksum;
    private FileStatus status;
    private UUID ownerId;
    private UUID parentFolderId;
    private String storageLocation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public enum FileStatus {
        PENDING,
        ACTIVE,
        DELETED
    }

    public void markAsDeleted() {
        this.status = FileStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(UUID userId) {
        return this.ownerId != null && this.ownerId.equals(userId);
    }

    public boolean isActive() {
        return FileStatus.ACTIVE.equals(this.status);
    }
}


