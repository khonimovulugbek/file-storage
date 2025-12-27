package com.file_storage.domain.service;

import com.file_storage.domain.model.storage.StorageNode;

import java.util.List;

/**
 * Strategy interface for node selection algorithms
 */
public interface NodeSelectionStrategy {
    
    /**
     * Select optimal node from available nodes
     * @param availableNodes List of available nodes
     * @return Selected node
     */
    StorageNode selectNode(List<StorageNode> availableNodes);
}
