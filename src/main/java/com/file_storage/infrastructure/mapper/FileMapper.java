package com.file_storage.infrastructure.mapper;

import com.file_storage.domain.model.File;
import com.file_storage.infrastructure.persistence.entity.file.FileMetaDataEntity;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {
    
    public File toDomain(FileMetaDataEntity entity) {
        if (entity == null) return null;
        
        return File.builder()
                .id(entity.getId())
                .name(entity.getName())
                .size(entity.getSize())
                .contentType(entity.getContentType())
                .checksum(entity.getChecksum())
                .status(File.FileStatus.valueOf(entity.getStatus().name()))
                .ownerId(entity.getOwnerId())
                .parentFolderId(entity.getParentFolderId())
                .storageLocation(entity.getStorageLocation())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
    
    public FileMetaDataEntity toEntity(File domain) {
        if (domain == null) return null;
        
        return FileMetaDataEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .size(domain.getSize())
                .contentType(domain.getContentType())
                .checksum(domain.getChecksum())
                .status(FileMetaDataEntity.FileStatus.valueOf(domain.getStatus().name()))
                .ownerId(domain.getOwnerId())
                .parentFolderId(domain.getParentFolderId())
                .storageLocation(domain.getStorageLocation())
                .deletedAt(domain.getDeletedAt())
                .build();
    }
}
