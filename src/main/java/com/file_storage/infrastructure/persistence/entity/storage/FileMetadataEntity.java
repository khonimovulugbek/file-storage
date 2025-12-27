package com.file_storage.infrastructure.persistence.entity.storage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for file metadata storage
 * Maps to file_metadata table
 */
@Entity
@Table(name = "file_metadata", indexes = {
    @Index(name = "idx_checksum", columnList = "checksum_hash", unique = true),
    @Index(name = "idx_owner", columnList = "owner_id"),
    @Index(name = "idx_storage_node", columnList = "storage_node_id"),
    @Index(name = "idx_uploaded_at", columnList = "uploaded_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataEntity {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
    // File metadata
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    // Checksum
    @Column(name = "checksum_algorithm", nullable = false, length = 20)
    private String checksumAlgorithm;
    
    @Column(name = "checksum_hash", nullable = false, length = 128, unique = true)
    private String checksumHash;
    
    // Storage reference (ENCRYPTED)
    @Column(name = "storage_type", nullable = false, length = 20)
    private String storageType;
    
    @Column(name = "storage_node_id", nullable = false, length = 50)
    private String storageNodeId;
    
    @Column(name = "encrypted_path", nullable = false, columnDefinition = "TEXT")
    private String encryptedPath;  // AES-256 encrypted
    
    @Column(name = "encryption_key_ref", nullable = false, length = 100)
    private String encryptionKeyRef;
    
    @Column(name = "bucket_name", length = 100)
    private String bucketName;
    
    @Column(name = "region", length = 50)
    private String region;
    
    // Ownership and timestamps
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
    
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
}
