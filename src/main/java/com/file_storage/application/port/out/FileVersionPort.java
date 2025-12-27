package com.file_storage.application.port.out;

import com.file_storage.domain.model.FileVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileVersionPort {
    FileVersion save(FileVersion version);
    Optional<FileVersion> findById(UUID versionId);
    List<FileVersion> findByFileId(UUID fileId);
    Optional<FileVersion> findCurrentVersion(UUID fileId);
    void markAllAsOld(UUID fileId);
}
