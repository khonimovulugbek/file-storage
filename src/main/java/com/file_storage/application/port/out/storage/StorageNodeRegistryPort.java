package com.file_storage.application.port.out.storage;

import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageType;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for storage node registry
 * Manages available storage servers for horizontal scaling
 */
public interface StorageNodeRegistryPort {
    
    /**
     * Find all available nodes for given storage type
     * @param storageType Type of storage (MINIO, S3, SFTP)
     * @return List of available nodes
     */
    List<StorageNode> findAvailableNodes(StorageType storageType);
    
    /**
     * Find node by ID
     * @param nodeId Node identifier
     * @return Optional storage node
     */
    Optional<StorageNode> findById(String nodeId);
    
    /**
     * Register new storage node
     * @param node Storage node to register
     * @return Registered node
     */
    StorageNode registerNode(StorageNode node);
    
    /**
     * Update node status
     * @param nodeId Node identifier
     * @param status New status
     */
    void updateNodeStatus(String nodeId, StorageNode.NodeStatus status);
    
    /**
     * Update node capacity
     * @param nodeId Node identifier
     * @param usedCapacityGb Used capacity in GB
     * @param fileCount Number of files
     */
    void updateNodeCapacity(String nodeId, long usedCapacityGb, long fileCount);
    
    /**
     * Find all nodes (for admin purposes)
     * @return List of all nodes
     */
    List<StorageNode> findAll();
}
