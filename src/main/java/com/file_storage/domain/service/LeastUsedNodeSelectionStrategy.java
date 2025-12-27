package com.file_storage.domain.service;

import com.file_storage.domain.model.storage.StorageNode;

import java.util.Comparator;
import java.util.List;

/**
 * Node selection strategy: Select node with least used capacity
 * Best for load balancing across nodes
 */
public class LeastUsedNodeSelectionStrategy implements NodeSelectionStrategy {
    
    @Override
    public StorageNode selectNode(List<StorageNode> availableNodes) {
        return availableNodes.stream()
            .min(Comparator.comparing(StorageNode::getUsedCapacityPercent))
            .orElseThrow(() -> new IllegalStateException("No available nodes"));
    }
}
