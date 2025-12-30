package com.file_storage.domain.model.storage;

import lombok.Builder;

@Builder
public record StorageNode(
        String nodeId,
        StorageType storageType,
        String nodeUrl,
        String publicNodeUrl,
        String accessKey,
        String secretKey,
        String bucket,
        StorageNodeStatus status
) {
    public boolean isAvailable() {
        return status == StorageNodeStatus.ACTIVE;
    }
}
