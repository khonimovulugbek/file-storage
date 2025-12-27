package com.file_storage.application.port.out;

import com.file_storage.domain.model.UploadSession;

import java.util.Optional;
import java.util.UUID;

public interface UploadSessionPort {
    UploadSession save(UploadSession session);
    Optional<UploadSession> findById(UUID sessionId);
    Optional<UploadSession> findByIdAndUserId(UUID sessionId, UUID userId);
    void delete(UUID sessionId);
    void deleteExpiredSessions();
}
