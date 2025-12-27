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
public class Folder {
    private UUID id;
    private String name;
    private UUID parentFolderId;
    private UUID ownerId;
    private String path;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isOwnedBy(UUID userId) {
        return this.ownerId != null && this.ownerId.equals(userId);
    }

    public boolean isRootFolder() {
        return this.parentFolderId == null;
    }
}
