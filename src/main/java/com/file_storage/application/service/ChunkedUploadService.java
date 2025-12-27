package com.file_storage.application.service;

import com.file_storage.application.port.in.ChunkedUploadUseCase;
import com.file_storage.application.port.out.*;
import com.file_storage.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChunkedUploadService implements ChunkedUploadUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final FileChunkPort fileChunkPort;
    private final FileStoragePort fileStoragePort;
    private final FilePort filePort;
    private final MessageQueuePort messageQueuePort;
    private final CachePort cachePort;

    private static final int SESSION_EXPIRY_HOURS = 24;

    @Override
    @Transactional
    public UploadSession initiateUpload(String fileName, Long totalSize, Integer totalChunks,
                                       String contentType, UUID userId, UUID folderId) {
        UploadSession session = UploadSession.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .folderId(folderId)
                .fileName(fileName)
                .totalSize(totalSize)
                .totalChunks(totalChunks)
                .uploadedChunks(0)
                .contentType(contentType)
                .status(UploadSession.SessionStatus.INITIATED)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(SESSION_EXPIRY_HOURS))
                .build();

        UploadSession saved = uploadSessionPort.save(session);
        log.info("Upload session initiated: {} for user: {}", saved.getId(), userId);

        return saved;
    }

    @Override
    @Transactional
    public FileChunk uploadChunk(UUID sessionId, Integer chunkNumber, InputStream chunkData,
                                Long chunkSize, String checksum, UUID userId) {
        UploadSession session = uploadSessionPort.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Upload session not found"));

        if (session.isExpired()) {
            throw new RuntimeException("Upload session expired");
        }

        if (chunkNumber >= session.getTotalChunks()) {
            throw new RuntimeException("Invalid chunk number");
        }

        String storageLocation = fileStoragePort.uploadFile(
                chunkData,
                String.format("%s_chunk_%d", session.getFileName(), chunkNumber),
                "application/octet-stream",
                chunkSize,
                userId.toString()
        );

        FileChunk chunk = FileChunk.builder()
                .id(UUID.randomUUID())
                .uploadSessionId(sessionId)
                .chunkNumber(chunkNumber)
                .totalChunks(session.getTotalChunks())
                .chunkSize(chunkSize)
                .checksum(checksum)
                .storageLocation(storageLocation)
                .status(FileChunk.ChunkStatus.COMPLETED)
                .uploadedAt(LocalDateTime.now())
                .build();

        FileChunk saved = fileChunkPort.save(chunk);

        session.setUploadedChunks(session.getUploadedChunks() + 1);
        session.setStatus(UploadSession.SessionStatus.IN_PROGRESS);
        uploadSessionPort.save(session);

        cachePort.delete("upload:session:" + sessionId);

        log.info("Chunk {} uploaded for session: {}", chunkNumber, sessionId);

        return saved;
    }

    @Override
    public UploadSession getUploadSession(UUID sessionId, UUID userId) {
        String cacheKey = "upload:session:" + sessionId;
        Object cached = cachePort.get(cacheKey);

        if (cached instanceof UploadSession) {
            return (UploadSession) cached;
        }

        UploadSession session = uploadSessionPort.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Upload session not found"));

        return session;
    }

    @Override
    public List<FileChunk> getUploadedChunks(UUID sessionId, UUID userId) {
        uploadSessionPort.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Upload session not found"));

        return fileChunkPort.findByUploadSessionId(sessionId);
    }

    @Override
    @Transactional
    public void completeUpload(UUID sessionId, UUID userId) {
        UploadSession session = uploadSessionPort.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Upload session not found"));

        if (!session.isCompleted()) {
            throw new RuntimeException("Not all chunks uploaded");
        }

        List<FileChunk> chunks = fileChunkPort.findCompletedChunksBySessionId(sessionId);
        if (chunks.size() != session.getTotalChunks()) {
            throw new RuntimeException("Missing chunks");
        }

        String finalStorageLocation = assembleChunks(chunks, session);

        File file = File.builder()
                .id(UUID.randomUUID())
                .name(session.getFileName())
                .size(session.getTotalSize())
                .contentType(session.getContentType())
                .status(File.FileStatus.ACTIVE)
                .ownerId(userId)
                .parentFolderId(session.getFolderId())
                .storageLocation(finalStorageLocation)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        File saved = filePort.save(file);

        session.setStatus(UploadSession.SessionStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        uploadSessionPort.save(session);

        cleanupChunks(chunks);

        messageQueuePort.publishFileUploadedEvent(saved.getId().toString(), userId.toString());
        messageQueuePort.publishVirusScanRequest(saved.getId().toString(), finalStorageLocation);

        cachePort.deletePattern("files:user:" + userId + ":*");

        log.info("Upload completed for session: {}, file: {}", sessionId, saved.getId());
    }

    @Override
    @Transactional
    public void cancelUpload(UUID sessionId, UUID userId) {
        UploadSession session = uploadSessionPort.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Upload session not found"));

        List<FileChunk> chunks = fileChunkPort.findByUploadSessionId(sessionId);
        cleanupChunks(chunks);

        session.setStatus(UploadSession.SessionStatus.FAILED);
        uploadSessionPort.save(session);

        cachePort.delete("upload:session:" + sessionId);

        log.info("Upload cancelled for session: {}", sessionId);
    }

    @Override
    public List<Integer> getMissingChunks(UUID sessionId, UUID userId) {
        UploadSession session = uploadSessionPort.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Upload session not found"));

        List<FileChunk> uploadedChunks = fileChunkPort.findByUploadSessionId(sessionId);
        List<Integer> uploadedNumbers = uploadedChunks.stream()
                .map(FileChunk::getChunkNumber)
                .toList();

        return IntStream.range(0, session.getTotalChunks())
                .filter(i -> !uploadedNumbers.contains(i))
                .boxed()
                .toList();
    }

    private String assembleChunks(List<FileChunk> chunks, UploadSession session) {
        log.info("Assembling {} chunks for session: {}", chunks.size(), session.getId());
        return "assembled/" + session.getFileName();
    }

    private void cleanupChunks(List<FileChunk> chunks) {
        for (FileChunk chunk : chunks) {
            try {
                fileStoragePort.deleteFile(chunk.getStorageLocation());
            } catch (Exception e) {
                log.error("Failed to cleanup chunk: {}", chunk.getId(), e);
            }
        }
        if (!chunks.isEmpty()) {
            fileChunkPort.deleteBySessionId(chunks.get(0).getUploadSessionId());
        }
    }
}
