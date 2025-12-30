package com.file_storage.domain.model.storage;

import lombok.Builder;

@Builder
public record StorageReference(
        StorageType storageType,
        String storageNodeId,
        String absolutePath,
        String path,
        String bucket,
        String region
) {
}
