package com.file_storage.infrastructure.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class InitiateUploadRequest {
    @NotBlank(message = "File name is required")
    private String fileName;

    @NotNull(message = "Total size is required")
    @Min(value = 1, message = "Total size must be greater than 0")
    private Long totalSize;

    @NotNull(message = "Total chunks is required")
    @Min(value = 1, message = "Total chunks must be at least 1")
    private Integer totalChunks;

    @NotBlank(message = "Content type is required")
    private String contentType;

    private UUID folderId;
}
