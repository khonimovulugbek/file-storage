package com.file_storage.infrastructure.persistence.entity.storage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity for storage node registry
 * Maps to storage_nodes table
 */
@Entity
@Table(name = "storage_nodes", indexes = {
    @Index(name = "idx_storage_type_status", columnList = "storage_type, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageNodeEntity {
    
    @Id
    @Column(name = "node_id", length = 50)
    private String nodeId;
    
    @Column(name = "storage_type", nullable = false, length = 20)
    private String storageType;
    
    @Column(name = "node_url", nullable = false, length = 500)
    private String nodeUrl;
    
    @Column(name = "access_key")
    private String accessKey;  // Encrypted
    
    @Column(name = "secret_key", columnDefinition = "TEXT")
    private String secretKey;  // Encrypted
    
    // Capacity tracking
    @Column(name = "total_capacity_gb")
    private Long totalCapacityGb;
    
    @Column(name = "used_capacity_gb")
    private Long usedCapacityGb;
    
    @Column(name = "file_count")
    private Long fileCount;
    
    // Status
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "health_check_url", length = 500)
    private String healthCheckUrl;
    
    @Column(name = "last_health_check")
    private LocalDateTime lastHealthCheck;
    
    // Metadata
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
