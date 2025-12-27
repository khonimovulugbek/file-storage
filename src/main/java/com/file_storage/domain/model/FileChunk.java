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
public class FileChunk {
    private UUID id;
    private UUID uploadSessionId;
    private UUID fileId;
    private Integer chunkNumber;
    private Integer totalChunks;
    private Long chunkSize;
    private String checksum;
    private String storageLocation;
    private ChunkStatus status;
    private LocalDateTime uploadedAt;

    public enum ChunkStatus {
        PENDING,
        UPLOADING,
        COMPLETED,
        FAILED
    }

    public boolean isLastChunk() {
        return chunkNumber.equals(totalChunks - 1);
    }

    public boolean isCompleted() {
        return ChunkStatus.COMPLETED.equals(this.status);
    }
}
