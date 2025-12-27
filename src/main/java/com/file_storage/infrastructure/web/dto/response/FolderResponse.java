package com.file_storage.infrastructure.web.dto.response;

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
public class FolderResponse {
    private UUID id;
    private String name;
    private UUID parentFolderId;
    private UUID ownerId;
    private String path;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
