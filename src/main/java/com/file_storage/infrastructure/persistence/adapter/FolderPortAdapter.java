package com.file_storage.infrastructure.persistence.adapter;

import com.file_storage.application.port.out.FolderPort;
import com.file_storage.domain.model.Folder;
import com.file_storage.infrastructure.mapper.FolderMapper;
import com.file_storage.infrastructure.persistence.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FolderPortAdapter implements FolderPort {

    private final FolderRepository folderRepository;
    private final FolderMapper folderMapper;

    @Override
    public Folder save(Folder folder) {
        return folderMapper.toDomain(folderRepository.save(folderMapper.toEntity(folder)));
    }

    @Override
    public Optional<Folder> findById(UUID folderId) {
        return folderRepository.findById(folderId).map(folderMapper::toDomain);
    }

    @Override
    public Optional<Folder> findByIdAndOwnerId(UUID folderId, UUID userId) {
        return folderRepository.findByIdAndOwnerId(folderId, userId).map(folderMapper::toDomain);
    }

    @Override
    public List<Folder> findByOwnerId(UUID userId) {
        return folderRepository.findByOwnerId(userId)
                .stream()
                .map(folderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Folder> findByParentFolderId(UUID parentFolderId) {
        return folderRepository.findByParentFolderId(parentFolderId)
                .stream()
                .map(folderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID folderId) {
        folderRepository.deleteById(folderId);
    }
}
