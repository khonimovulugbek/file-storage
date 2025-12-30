package com.file_storage.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import com.file_storage.domain.model.storage.StorageNodeStatus;
import com.file_storage.domain.model.storage.StorageType;

import java.util.List;

@ConfigurationProperties(prefix = "file.node")
public record StorageNodesConfig(
        List<NodeConfig> nodes,
        String defaultNode
) {
    public record NodeConfig(
            String id,
            StorageType type,
            String endpoint,
            String publicEndpoint,
            String accessKey,
            String secretKey,
            String bucket,
            StorageNodeStatus status
    ) {
    }
}
