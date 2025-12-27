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
public class FileResponse {
    private UUID id;
    private String name;
    private Long size;
    private String contentType;
    private String status;
    private UUID ownerId;
    private UUID parentFolderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String downloadUrl;
}
