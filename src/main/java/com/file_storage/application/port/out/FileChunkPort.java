package com.file_storage.application.port.out;

import com.file_storage.domain.model.FileChunk;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileChunkPort {
    FileChunk save(FileChunk chunk);
    Optional<FileChunk> findById(UUID chunkId);
    List<FileChunk> findByUploadSessionId(UUID sessionId);
    List<FileChunk> findCompletedChunksBySessionId(UUID sessionId);
    void deleteBySessionId(UUID sessionId);
}
