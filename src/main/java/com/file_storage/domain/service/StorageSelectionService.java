package com.file_storage.domain.service;

import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageType;

import java.util.List;

/**
 * Domain service for selecting optimal storage node
 * Implements strategy pattern for node selection
 */
public class StorageSelectionService {
    
    private final NodeSelectionStrategy strategy;
    
    public StorageSelectionService(NodeSelectionStrategy strategy) {
        this.strategy = strategy;
    }
    
    /**
     * Select optimal storage node from available nodes
     * @param availableNodes List of available nodes
     * @param preferredType Preferred storage type (optional)
     * @return Selected storage node
     */
    public StorageNode selectNode(List<StorageNode> availableNodes, StorageType preferredType) {
        if (availableNodes == null || availableNodes.isEmpty()) {
            throw new IllegalStateException("No available storage nodes");
        }
        
        // Filter by preferred type if specified
        List<StorageNode> candidateNodes = preferredType != null
            ? availableNodes.stream()
                .filter(node -> node.storageType() == preferredType)
                .filter(StorageNode::isAvailable)
                .toList()
            : availableNodes.stream()
                .filter(StorageNode::isAvailable)
                .toList();
        
        if (candidateNodes.isEmpty()) {
            throw new IllegalStateException("No available nodes for storage type: " + preferredType);
        }
        
        return strategy.selectNode(candidateNodes);
    }
}
