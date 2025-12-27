package com.file_storage.domain.service;

import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageType;

import java.util.Comparator;
import java.util.List;

/**
 * Domain service for selecting optimal storage node
 * Uses least-used capacity strategy (multi-instance safe)
 */
public class StorageSelectionService {
    
    /**
     * Select optimal storage node from available nodes
     * Strategy: Select node with least used capacity percentage
     * 
     * @param availableNodes List of available nodes
     * @param preferredType Preferred storage type (optional)
     * @return Selected storage node
     */
    public StorageNode selectNode(List<StorageNode> availableNodes, StorageType preferredType) {
        if (availableNodes == null || availableNodes.isEmpty()) {
            throw new IllegalStateException("No available storage nodes");
        }
        
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
        
        return candidateNodes.stream()
            .min(Comparator.comparing(StorageNode::getUsedCapacityPercent))
            .orElseThrow(() -> new IllegalStateException("No available nodes"));
    }
}
