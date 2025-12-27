package com.file_storage.infrastructure.persistence.mapper.storage;

import com.file_storage.domain.model.storage.*;
import com.file_storage.infrastructure.persistence.entity.storage.FileMetadataEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between FileAggregate domain model and FileMetadataEntity
 */
@Component
public class FileMetadataMapper {
    
    public FileAggregate toDomain(FileMetadataEntity entity) {
        if (entity == null) return null;
        
        // Map checksum
        FileChecksum checksum = new FileChecksum(
            FileChecksum.ChecksumAlgorithm.valueOf(entity.getChecksumAlgorithm()),
            entity.getChecksumHash()
        );
        
        // Map storage reference
        StorageReference storageReference = StorageReference.builder()
            .storageType(StorageType.valueOf(entity.getStorageType()))
            .storageNodeId(entity.getStorageNodeId())
            .encryptedPath(entity.getEncryptedPath())
            .encryptionKeyRef(entity.getEncryptionKeyRef())
            .bucket(entity.getBucketName())
            .region(entity.getRegion())
            .build();
        
        // Map file metadata
        FileMetadata metadata = FileMetadata.builder()
            .fileName(entity.getFileName())
            .contentType(entity.getContentType())
            .fileSize(entity.getFileSize())
            .ownerId(entity.getOwnerId())
            .uploadedAt(entity.getUploadedAt())
            .updatedAt(entity.getUpdatedAt())
            .status(FileMetadata.FileStatus.valueOf(entity.getStatus()))
            .build();
        
        // Build aggregate
        return FileAggregate.builder()
            .fileId(FileId.of(entity.getId()))
            .metadata(metadata)
            .storageReference(storageReference)
            .checksum(checksum)
            .build();
    }
    
    public FileMetadataEntity toEntity(FileAggregate aggregate) {
        if (aggregate == null) return null;
        
        return FileMetadataEntity.builder()
            .id(aggregate.getFileId().getValue())
            .fileName(aggregate.getMetadata().getFileName())
            .contentType(aggregate.getMetadata().getContentType())
            .fileSize(aggregate.getMetadata().getFileSize())
            .checksumAlgorithm(aggregate.getChecksum().getAlgorithm().name())
            .checksumHash(aggregate.getChecksum().getHash())
            .storageType(aggregate.getStorageReference().getStorageType().name())
            .storageNodeId(aggregate.getStorageReference().getStorageNodeId())
            .encryptedPath(aggregate.getStorageReference().getEncryptedPath())
            .encryptionKeyRef(aggregate.getStorageReference().getEncryptionKeyRef())
            .bucketName(aggregate.getStorageReference().getBucket())
            .region(aggregate.getStorageReference().getRegion())
            .ownerId(aggregate.getMetadata().getOwnerId())
            .uploadedAt(aggregate.getMetadata().getUploadedAt())
            .updatedAt(aggregate.getMetadata().getUpdatedAt())
            .status(aggregate.getMetadata().getStatus().name())
            .build();
    }
}
