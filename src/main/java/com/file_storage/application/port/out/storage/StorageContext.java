package com.file_storage.application.port.out.storage;

import lombok.Builder;
import com.file_storage.domain.model.storage.StorageNode;

@Builder
public record StorageContext(
        String fileName,
        String contentType,
        long fileSize,
        String bucket,
        StorageNode storageNode,
        String basePath
) {

    public String fileName(){
        return this.fileName.replaceAll("\\s+", "_");
    }
}
