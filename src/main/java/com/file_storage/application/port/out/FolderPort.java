package com.file_storage.application.port.out;

import com.file_storage.domain.model.Folder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderPort {
    Folder save(Folder folder);
    Optional<Folder> findById(UUID folderId);
    Optional<Folder> findByIdAndOwnerId(UUID folderId, UUID userId);
    List<Folder> findByOwnerId(UUID userId);
    List<Folder> findByParentFolderId(UUID parentFolderId);
    void delete(UUID folderId);
}
