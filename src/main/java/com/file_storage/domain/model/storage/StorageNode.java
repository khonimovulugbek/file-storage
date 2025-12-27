package com.file_storage.domain.model.storage;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Value Object representing a storage server node
 * Used for horizontal scaling - multiple nodes can be registered
 */
@Builder
public record StorageNode(String nodeId, StorageType storageType, String nodeUrl, String accessKey,
                          String secretKey, Long totalCapacityGb, Long usedCapacityGb, Long fileCount,
                          NodeStatus status, String healthCheckUrl, LocalDateTime lastHealthCheck) {
    public StorageNode(String nodeId, StorageType storageType, String nodeUrl, String accessKey,
                       String secretKey, Long totalCapacityGb, Long usedCapacityGb, Long fileCount,
                       NodeStatus status, String healthCheckUrl, LocalDateTime lastHealthCheck) {
        if (nodeId == null || nodeId.isBlank()) {
            throw new IllegalArgumentException("Node ID cannot be null or empty");
        }
        if (storageType == null) {
            throw new IllegalArgumentException("Storage type cannot be null");
        }
        if (nodeUrl == null || nodeUrl.isBlank()) {
            throw new IllegalArgumentException("Node URL cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Node status cannot be null");
        }

        this.nodeId = nodeId;
        this.storageType = storageType;
        this.nodeUrl = nodeUrl;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.totalCapacityGb = totalCapacityGb;
        this.usedCapacityGb = usedCapacityGb != null ? usedCapacityGb : 0L;
        this.fileCount = fileCount != null ? fileCount : 0L;
        this.status = status;
        this.healthCheckUrl = healthCheckUrl;
        this.lastHealthCheck = lastHealthCheck;
    }

    public boolean isAvailable() {
        return status == NodeStatus.ACTIVE && !isFull();
    }

    public boolean isFull() {
        if (totalCapacityGb == null || totalCapacityGb == 0) {
            return false;
        }
        return getUsedCapacityPercent() >= 95.0;
    }

    public double getUsedCapacityPercent() {
        if (totalCapacityGb == null || totalCapacityGb == 0) {
            return 0.0;
        }
        return (usedCapacityGb.doubleValue() / totalCapacityGb.doubleValue()) * 100.0;
    }

    public long getAvailableCapacityGb() {
        if (totalCapacityGb == null) {
            return Long.MAX_VALUE;
        }
        return totalCapacityGb - usedCapacityGb;
    }

    public enum NodeStatus {
        ACTIVE,
        FULL,
        MAINTENANCE,
        OFFLINE
    }
}
