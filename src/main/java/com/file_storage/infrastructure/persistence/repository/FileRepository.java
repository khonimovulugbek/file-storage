package com.file_storage.infrastructure.persistence.repository;

import com.file_storage.infrastructure.persistence.entity.file.FileMetaDataEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileMetaDataEntity, UUID> {
    List<FileMetaDataEntity> findByOwnerId(UUID ownerId);
    
    Page<FileMetaDataEntity> findByOwnerId(UUID ownerId, Pageable pageable);
    
    List<FileMetaDataEntity> findByParentFolderId(UUID folderId);
    
    Page<FileMetaDataEntity> findByParentFolderId(UUID folderId, Pageable pageable);
    
    @Query("SELECT f FROM FileMetaDataEntity f WHERE f.ownerId = :ownerId AND f.status = 'ACTIVE'")
    List<FileMetaDataEntity> findActiveFilesByOwner(@Param("ownerId") UUID ownerId);
    
    @Query("SELECT f FROM FileMetaDataEntity f WHERE f.ownerId = :ownerId AND f.name LIKE %:name%")
    List<FileMetaDataEntity> searchByName(@Param("ownerId") UUID ownerId, @Param("name") String name);
    
    Optional<FileMetaDataEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
}
