package com.file_storage.infrastructure.persistence.repository.storage;

import com.file_storage.infrastructure.persistence.entity.storage.FileMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for FileMetadataEntity
 */
@Repository
public interface FileMetadataJpaRepository extends JpaRepository<FileMetadataEntity, UUID> {
    
    Optional<FileMetadataEntity> findByChecksumHash(String checksumHash);
    
    Optional<FileMetadataEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
}
