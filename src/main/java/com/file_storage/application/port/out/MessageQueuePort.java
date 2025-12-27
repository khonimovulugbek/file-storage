package com.file_storage.application.port.out;

public interface MessageQueuePort {
    void publishFileUploadedEvent(String fileId, String userId);
    void publishFileDeletedEvent(String fileId, String userId);
    void publishSyncEvent(String eventType, String payload);
    void publishNotificationEvent(String userId, String message);
    void publishVirusScanRequest(String fileId, String storageLocation);
}
