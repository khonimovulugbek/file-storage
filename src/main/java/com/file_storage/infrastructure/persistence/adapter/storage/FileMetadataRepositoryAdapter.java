package com.file_storage.infrastructure.persistence.adapter.storage;

import com.file_storage.application.port.out.storage.FileMetadataRepositoryPort;
import com.file_storage.domain.model.storage.*;
import com.file_storage.infrastructure.persistence.entity.storage.FileMetadataEntity;
import com.file_storage.infrastructure.persistence.mapper.storage.FileMetadataMapper;
import com.file_storage.infrastructure.persistence.repository.storage.FileMetadataJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing FileMetadataRepositoryPort
 * Bridges domain layer with JPA persistence
 */
@Component
@RequiredArgsConstructor
public class FileMetadataRepositoryAdapter implements FileMetadataRepositoryPort {
    
    private final FileMetadataJpaRepository jpaRepository;
    private final FileMetadataMapper mapper;
    
    @Override
    public FileAggregate save(FileAggregate aggregate) {
        FileMetadataEntity entity = mapper.toEntity(aggregate);
        FileMetadataEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<FileAggregate> findById(FileId fileId) {
        return jpaRepository.findById(fileId.getValue())
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<FileAggregate> findByChecksum(FileChecksum checksum) {
        return jpaRepository.findByChecksumHash(checksum.getHash())
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<FileAggregate> findByIdAndOwner(FileId fileId, UUID ownerId) {
        return jpaRepository.findByIdAndOwnerId(fileId.getValue(), ownerId)
            .map(mapper::toDomain);
    }
    
    @Override
    public void delete(FileId fileId) {
        jpaRepository.findById(fileId.getValue()).ifPresent(entity -> {
            entity.setStatus("DELETED");
            jpaRepository.save(entity);
        });
    }
    
    @Override
    public boolean exists(FileId fileId) {
        return jpaRepository.existsById(fileId.getValue());
    }
}
