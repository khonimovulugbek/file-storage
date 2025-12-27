package com.file_storage.infrastructure.persistence.adapter;

import com.file_storage.application.port.out.FilePort;
import com.file_storage.domain.model.File;
import com.file_storage.infrastructure.mapper.FileMapper;
import com.file_storage.infrastructure.persistence.entity.file.FileMetaDataEntity;
import com.file_storage.infrastructure.persistence.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilePortAdapter implements FilePort {

    private final FileRepository fileRepository;
    private final FileMapper fileMapper;

    @Override
    public File save(File file) {
        FileMetaDataEntity entity = fileMapper.toEntity(file);
        FileMetaDataEntity saved = fileRepository.save(entity);
        return fileMapper.toDomain(saved);
    }

    @Override
    public Optional<File> findById(UUID fileId, UUID userId) {
        return fileRepository.findByIdAndOwnerId(fileId, userId)
                .map(fileMapper::toDomain);
    }

    @Override
    public List<File> findActiveFilesByOwner(UUID userId) {
        return fileRepository.findActiveFilesByOwner(userId)
                .stream()
                .map(fileMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<File> findByParentFolderId(UUID folderId) {
        return fileRepository.findByParentFolderId(folderId)
                .stream()
                .map(fileMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<File> searchByName(UUID userId, String query) {
        return fileRepository.searchByName(userId, query)
                .stream()
                .map(fileMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID fileId) {
        fileRepository.findById(fileId).ifPresent(entity -> {
            entity.setStatus(FileMetaDataEntity.FileStatus.DELETED);
            fileRepository.save(entity);
        });
    }
}
