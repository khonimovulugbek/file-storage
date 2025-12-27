package com.file_storage.infrastructure.persistence.mapper.storage;

import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageType;
import com.file_storage.infrastructure.persistence.entity.storage.StorageNodeEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between StorageNode domain model and StorageNodeEntity
 */
@Component
public class StorageNodeMapper {
    
    public StorageNode toDomain(StorageNodeEntity entity) {
        if (entity == null) return null;
        
        return StorageNode.builder()
            .nodeId(entity.getNodeId())
            .storageType(StorageType.valueOf(entity.getStorageType()))
            .nodeUrl(entity.getNodeUrl())
            .totalCapacityGb(entity.getTotalCapacityGb())
            .usedCapacityGb(entity.getUsedCapacityGb())
            .fileCount(entity.getFileCount())
            .status(StorageNode.NodeStatus.valueOf(entity.getStatus()))
            .healthCheckUrl(entity.getHealthCheckUrl())
            .lastHealthCheck(entity.getLastHealthCheck())
            .build();
    }
    
    public StorageNodeEntity toEntity(StorageNode node) {
        if (node == null) return null;
        
        return StorageNodeEntity.builder()
            .nodeId(node.nodeId())
            .storageType(node.storageType().name())
            .nodeUrl(node.nodeUrl())
            .totalCapacityGb(node.totalCapacityGb())
            .usedCapacityGb(node.usedCapacityGb())
            .fileCount(node.fileCount())
            .status(node.status().name())
            .healthCheckUrl(node.healthCheckUrl())
            .lastHealthCheck(node.lastHealthCheck())
            .build();
    }
}
