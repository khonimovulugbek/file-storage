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
public class SyncEvent {
    private UUID id;
    private UUID userId;
    private UUID resourceId;
    private ResourceType resourceType;
    private EventType eventType;
    private String deviceId;
    private LocalDateTime timestamp;
    private String payload;

    public enum ResourceType {
        FILE,
        FOLDER,
        PERMISSION
    }

    public enum EventType {
        CREATED,
        UPDATED,
        DELETED,
        MOVED,
        SHARED,
        UNSHARED
    }
}
