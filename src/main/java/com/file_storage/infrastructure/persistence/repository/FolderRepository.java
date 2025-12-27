package com.file_storage.infrastructure.persistence.repository;

import com.file_storage.infrastructure.persistence.entity.folder.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, UUID> {
    List<FolderEntity> findByOwnerId(UUID ownerId);
    List<FolderEntity> findByParentFolderId(UUID parentFolderId);
    Optional<FolderEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
    List<FolderEntity> findByOwnerIdAndParentFolderIdIsNull(UUID ownerId);
}
