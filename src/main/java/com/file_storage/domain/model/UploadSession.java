package com.file_storage.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadSession {
    private UUID id;
    private UUID userId;
    private UUID folderId;
    private String fileName;
    private Long totalSize;
    private Integer totalChunks;
    private Integer uploadedChunks;
    private String contentType;
    private SessionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime completedAt;

    public enum SessionStatus {
        INITIATED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        EXPIRED
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isCompleted() {
        return uploadedChunks.equals(totalChunks);
    }

    public double getProgress() {
        return (uploadedChunks.doubleValue() / totalChunks.doubleValue()) * 100;
    }
}
