package com.file_storage.application.port.in.storage;

import lombok.Builder;
import com.file_storage.domain.model.storage.FileId;

import java.io.InputStream;
import java.time.LocalDateTime;

public interface UploadFileUseCase {

    FileUploadResult upload(FileUploadCommand command);

    @Builder
    record FileUploadCommand(
            InputStream content,
            String fileName,
            String contentType,
            long fileSize,
            String ownerId
    ) {
    }

    @Builder
    record FileUploadResult(
            FileId fileId,
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


}
