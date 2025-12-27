package com.file_storage.infrastructure.persistence.mapper.storage;

import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageType;
import com.file_storage.domain.service.CredentialEncryptionService;
import com.file_storage.infrastructure.persistence.entity.storage.StorageNodeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper between StorageNode domain model and StorageNodeEntity
 * Handles automatic encryption/decryption of credentials
 */
@Component
@RequiredArgsConstructor
public class StorageNodeMapper {
    
    private final CredentialEncryptionService credentialEncryptionService;
    
    public StorageNode toDomain(StorageNodeEntity entity) {
        if (entity == null) return null;
        
        String decryptedAccessKey = credentialEncryptionService.decryptCredential(entity.getAccessKey());
        String decryptedSecretKey = credentialEncryptionService.decryptCredential(entity.getSecretKey());
        
        return StorageNode.builder()
            .nodeId(entity.getNodeId())
            .storageType(StorageType.valueOf(entity.getStorageType()))
            .nodeUrl(entity.getNodeUrl())
            .accessKey(decryptedAccessKey)
            .secretKey(decryptedSecretKey)
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
        
        String encryptedAccessKey = credentialEncryptionService.encryptCredential(node.accessKey());
        String encryptedSecretKey = credentialEncryptionService.encryptCredential(node.secretKey());
        
        return StorageNodeEntity.builder()
            .nodeId(node.nodeId())
            .storageType(node.storageType().name())
            .nodeUrl(node.nodeUrl())
            .accessKey(encryptedAccessKey)
            .secretKey(encryptedSecretKey)
            .totalCapacityGb(node.totalCapacityGb())
            .usedCapacityGb(node.usedCapacityGb())
            .fileCount(node.fileCount())
            .status(node.status().name())
            .healthCheckUrl(node.healthCheckUrl())
            .lastHealthCheck(node.lastHealthCheck())
            .build();
    }
}
