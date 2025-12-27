package com.file_storage.infrastructure.persistence.repository.storage;

import com.file_storage.infrastructure.persistence.entity.storage.StorageNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for StorageNodeEntity
 */
@Repository
public interface StorageNodeJpaRepository extends JpaRepository<StorageNodeEntity, String> {
    
    List<StorageNodeEntity> findByStorageTypeAndStatus(String storageType, String status);
}
