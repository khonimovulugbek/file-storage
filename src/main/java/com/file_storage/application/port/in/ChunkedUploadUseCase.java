package com.file_storage.application.port.in;

import com.file_storage.domain.model.FileChunk;
import com.file_storage.domain.model.UploadSession;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface ChunkedUploadUseCase {
    UploadSession initiateUpload(String fileName, Long totalSize, Integer totalChunks, 
                                 String contentType, UUID userId, UUID folderId);
    FileChunk uploadChunk(UUID sessionId, Integer chunkNumber, InputStream chunkData, 
                         Long chunkSize, String checksum, UUID userId);
    UploadSession getUploadSession(UUID sessionId, UUID userId);
    List<FileChunk> getUploadedChunks(UUID sessionId, UUID userId);
    void completeUpload(UUID sessionId, UUID userId);
    void cancelUpload(UUID sessionId, UUID userId);
    List<Integer> getMissingChunks(UUID sessionId, UUID userId);
}
