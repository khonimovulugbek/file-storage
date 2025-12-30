package com.file_storage.infrastructure.persistence.adapter.storage;

import com.file_storage.application.port.out.storage.StorageNodeRegistryPort;
import com.file_storage.domain.exception.StorageNodeNotFoundException;
import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageNodeStatus;
import com.file_storage.domain.model.storage.StorageType;
import com.file_storage.infrastructure.annotation.Adapter;
import com.file_storage.infrastructure.persistence.mapper.storage.StorageNodeMapper;
import com.file_storage.infrastructure.properties.StorageNodesConfig;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Adapter
public class InMemoryStorageNodeRegistryAdapter implements StorageNodeRegistryPort {
    private final StorageNodesConfig storageNodesConfig;
    private final StorageNodeMapper storageNodeMapper;
    private final Random random = new SecureRandom();


    public InMemoryStorageNodeRegistryAdapter(StorageNodesConfig storageNodesConfig, StorageNodeMapper storageNodeMapper) {
        this.storageNodesConfig = storageNodesConfig;
        this.storageNodeMapper = storageNodeMapper;
    }

    @Override
    public List<StorageNode> findAvailableNodes(StorageType storageType) {
        List<StorageNode> storageNodes = findAll();
        if (storageNodes == null || storageNodes.isEmpty()) return Collections.emptyList();
        return storageNodes.stream().filter(s ->
                s.storageType() != null
                        && s.storageType() == storageType
                        && s.status() != null
                        && s.status() == StorageNodeStatus.ACTIVE).toList();
    }

    @Override
    public List<StorageNode> findAvailableNodes() {
        List<StorageNode> storageNodes = findAll();
        if (storageNodes == null || storageNodes.isEmpty()) return Collections.emptyList();
        return storageNodes.stream().filter(s ->
                s.storageType() != null
                        && s.status() != null
                        && s.status() == StorageNodeStatus.ACTIVE).toList();
    }

    @Override
    public List<StorageNode> findAll() {
        return storageNodeMapper.toDomain(storageNodesConfig.nodes());
    }

    @Override
    public StorageNode findRandomAvailableNode(StorageType storageType) {
        List<StorageNode> availableNodes = findAvailableNodes(storageType);
        if (availableNodes == null || availableNodes.isEmpty()) {
            throw new StorageNodeNotFoundException("No available storage nodes found for type: " + storageType);
        }
        int index = random.nextInt(availableNodes.size());
        return availableNodes.get(index);
    }

    @Override
    public StorageNode findRandomAvailableNode() {
        List<StorageNode> availableNodes = findAvailableNodes();
        if (availableNodes == null || availableNodes.isEmpty()) {
            throw new StorageNodeNotFoundException("No available storage nodes found");
        }
        int index = random.nextInt(availableNodes.size());
        return availableNodes.get(index);
    }

    @Override
    public StorageNode findById(String nodeId) {
        return findAll().stream()
                .filter(s -> s.nodeId().equals(nodeId))
                .findFirst()
                .orElseThrow(() -> new StorageNodeNotFoundException(nodeId));
    }
}
