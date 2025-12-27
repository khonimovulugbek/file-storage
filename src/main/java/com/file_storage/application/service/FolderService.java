package com.file_storage.application.service;

import com.file_storage.application.port.in.FolderUseCase;
import com.file_storage.application.port.out.FolderPort;
import com.file_storage.domain.model.Folder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FolderService implements FolderUseCase {

    private final FolderPort folderPort;

    @Override
    @Transactional
    public Folder createFolder(String name, UUID parentFolderId, UUID userId) {
        String path = buildPath(parentFolderId, name);

        Folder folder = Folder.builder()
                .id(UUID.randomUUID())
                .name(name)
                .parentFolderId(parentFolderId)
                .ownerId(userId)
                .path(path)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Folder saved = folderPort.save(folder);
        log.info("Folder created successfully: {}", saved.getId());

        return saved;
    }

    @Override
    public Folder getFolderById(UUID folderId, UUID userId) {
        return folderPort.findByIdAndOwnerId(folderId, userId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));
    }

    @Override
    public List<Folder> listUserFolders(UUID userId) {
        return folderPort.findByOwnerId(userId);
    }

    @Override
    public List<Folder> listSubFolders(UUID parentFolderId, UUID userId) {
        return folderPort.findByParentFolderId(parentFolderId)
                .stream()
                .filter(f -> f.getOwnerId().equals(userId))
                .toList();
    }

    @Override
    @Transactional
    public void deleteFolder(UUID folderId, UUID userId) {
        folderPort.findByIdAndOwnerId(folderId, userId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        folderPort.delete(folderId);
        log.info("Folder deleted successfully: {}", folderId);
    }

    @Override
    @Transactional
    public Folder updateFolder(UUID folderId, String newName, UUID userId) {
        Folder folder = folderPort.findByIdAndOwnerId(folderId, userId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        folder.setName(newName);
        String newPath = buildPath(folder.getParentFolderId(), newName);
        folder.setPath(newPath);
        folder.setUpdatedAt(LocalDateTime.now());

        Folder updated = folderPort.save(folder);
        log.info("Folder updated successfully: {}", folderId);

        return updated;
    }

    private String buildPath(UUID parentFolderId, String name) {
        if (parentFolderId == null) {
            return "/" + name;
        }

        Folder parent = folderPort.findById(parentFolderId)
                .orElseThrow(() -> new RuntimeException("Parent folder not found"));

        return parent.getPath() + "/" + name;
    }
}
