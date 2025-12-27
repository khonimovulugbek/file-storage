package com.file_storage.application.port.out;

import com.file_storage.domain.model.File;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FilePort {
    File save(File file);
    Optional<File> findById(UUID fileId, UUID userId);
    List<File> findActiveFilesByOwner(UUID userId);
    List<File> findByParentFolderId(UUID folderId);
    List<File> searchByName(UUID userId, String query);
    void delete(UUID fileId);
}
