package com.file_storage.domain.service;

import com.file_storage.domain.model.storage.StorageNode;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Node selection strategy: Round-robin distribution
 * Simple and fair distribution across nodes
 */
public class RoundRobinNodeSelectionStrategy implements NodeSelectionStrategy {
    
    private final AtomicInteger counter = new AtomicInteger(0);
    
    @Override
    public StorageNode selectNode(List<StorageNode> availableNodes) {
        int index = Math.abs(counter.getAndIncrement() % availableNodes.size());
        return availableNodes.get(index);
    }
}
