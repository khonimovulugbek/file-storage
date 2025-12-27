package com.file_storage.infrastructure.web.controller.storage;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileUploadResponse {
    private String fileId;
    private String fileName;
    private long fileSize;
    private String contentType;
    private String checksum;
    private String storageNodeId;
    private LocalDateTime uploadedAt;
    private boolean deduplicated;
}
