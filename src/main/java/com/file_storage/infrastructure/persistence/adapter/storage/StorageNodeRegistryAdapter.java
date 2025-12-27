package com.file_storage.infrastructure.persistence.adapter.storage;

import com.file_storage.application.port.out.storage.StorageNodeRegistryPort;
import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageType;
import com.file_storage.infrastructure.persistence.entity.storage.StorageNodeEntity;
import com.file_storage.infrastructure.persistence.mapper.storage.StorageNodeMapper;
import com.file_storage.infrastructure.persistence.repository.storage.StorageNodeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing StorageNodeRegistryPort
 * Manages storage node registry in database
 */
@Component
@RequiredArgsConstructor
public class StorageNodeRegistryAdapter implements StorageNodeRegistryPort {
    
    private final StorageNodeJpaRepository jpaRepository;
    private final StorageNodeMapper mapper;
    
    @Override
    public List<StorageNode> findAvailableNodes(StorageType storageType) {
        String type = storageType != null ? storageType.name() : null;
        String status = StorageNode.NodeStatus.ACTIVE.name();
        
        if (type != null) {
            return jpaRepository.findByStorageTypeAndStatus(type, status).stream()
                .map(mapper::toDomain)
                .filter(StorageNode::isAvailable)
                .toList();
        } else {
            return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .filter(StorageNode::isAvailable)
                .toList();
        }
    }
    
    @Override
    public Optional<StorageNode> findById(String nodeId) {
        return jpaRepository.findById(nodeId)
            .map(mapper::toDomain);
    }
    
    @Override
    public StorageNode registerNode(StorageNode node) {
        StorageNodeEntity entity = mapper.toEntity(node);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        StorageNodeEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public void updateNodeStatus(String nodeId, StorageNode.NodeStatus status) {
        jpaRepository.findById(nodeId).ifPresent(entity -> {
            entity.setStatus(status.name());
            entity.setUpdatedAt(LocalDateTime.now());
            jpaRepository.save(entity);
        });
    }
    
    @Override
    public void updateNodeCapacity(String nodeId, long usedCapacityGb, long fileCount) {
        jpaRepository.findById(nodeId).ifPresent(entity -> {
            entity.setUsedCapacityGb(usedCapacityGb);
            entity.setFileCount(fileCount);
            entity.setUpdatedAt(LocalDateTime.now());
            jpaRepository.save(entity);
        });
    }
    
    @Override
    public List<StorageNode> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .toList();
    }
}
