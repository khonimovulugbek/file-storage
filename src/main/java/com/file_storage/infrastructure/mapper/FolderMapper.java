package com.file_storage.infrastructure.mapper;

import com.file_storage.domain.model.Folder;
import com.file_storage.infrastructure.persistence.entity.folder.FolderEntity;
import org.springframework.stereotype.Component;

@Component
public class FolderMapper {
    
    public Folder toDomain(FolderEntity entity) {
        if (entity == null) return null;
        
        return Folder.builder()
                .id(entity.getId())
                .name(entity.getName())
                .parentFolderId(entity.getParentFolderId())
                .ownerId(entity.getOwnerId())
                .path(entity.getPath())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public FolderEntity toEntity(Folder domain) {
        if (domain == null) return null;
        
        return FolderEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .parentFolderId(domain.getParentFolderId())
                .ownerId(domain.getOwnerId())
                .path(domain.getPath())
                .build();
    }
}
