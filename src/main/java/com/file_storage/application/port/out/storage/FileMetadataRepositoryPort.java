package com.file_storage.application.port.out.storage;

import com.file_storage.domain.model.storage.FileAggregate;
import com.file_storage.domain.model.storage.FileChecksum;
import com.file_storage.domain.model.storage.FileId;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for file metadata persistence
 * Repository pattern for FileAggregate
 */
public interface FileMetadataRepositoryPort {
    
    /**
     * Save file aggregate to database
     * @param aggregate File aggregate to save
     * @return Saved file aggregate
     */
    FileAggregate save(FileAggregate aggregate);
    
    /**
     * Find file by ID
     * @param fileId File identifier
     * @return Optional file aggregate
     */
    Optional<FileAggregate> findById(FileId fileId);
    
    /**
     * Find file by checksum (for deduplication)
     * @param checksum File checksum
     * @return Optional file aggregate
     */
    Optional<FileAggregate> findByChecksum(FileChecksum checksum);
    
    /**
     * Find file by ID and owner (for authorization)
     * @param fileId File identifier
     * @param ownerId Owner user ID
     * @return Optional file aggregate
     */
    Optional<FileAggregate> findByIdAndOwner(FileId fileId, UUID ownerId);
    
    /**
     * Delete file metadata (soft delete)
     * @param fileId File identifier
     */
    void delete(FileId fileId);
    
    /**
     * Check if file exists
     * @param fileId File identifier
     * @return true if exists
     */
    boolean exists(FileId fileId);
}
