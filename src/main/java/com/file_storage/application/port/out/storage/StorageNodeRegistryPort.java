package com.file_storage.application.port.out.storage;

import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageType;

import java.util.List;

public interface StorageNodeRegistryPort {
    List<StorageNode> findAvailableNodes(StorageType storageType);

    List<StorageNode> findAvailableNodes();

    List<StorageNode> findAll();

    StorageNode findRandomAvailableNode(StorageType storageType);

    StorageNode findRandomAvailableNode();

    StorageNode findById(String nodeId);

}
