package com.file_storage.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.file_storage.domain.model.SyncEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    public void sendSyncEvent(String userId, SyncEvent event) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/sync",
                    event
            );
            log.debug("Sent sync event to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send sync event to user: {}", userId, e);
        }
    }

    public void broadcastFileChange(String fileId, String eventType, String userId) {
        try {
            Map<String, String> message = Map.of(
                    "fileId", fileId,
                    "eventType", eventType,
                    "userId", userId,
                    "timestamp", String.valueOf(System.currentTimeMillis())
            );
            messagingTemplate.convertAndSend("/topic/file-changes", message);
            log.debug("Broadcasted file change: {} - {}", fileId, eventType);
        } catch (Exception e) {
            log.error("Failed to broadcast file change", e);
        }
    }

    public void notifyUploadProgress(String userId, String sessionId, double progress) {
        try {
            Map<String, Object> message = Map.of(
                    "sessionId", sessionId,
                    "progress", progress,
                    "timestamp", System.currentTimeMillis()
            );
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/upload-progress",
                    message
            );
        } catch (Exception e) {
            log.error("Failed to notify upload progress", e);
        }
    }

    public void registerUserSession(String userId, String sessionId) {
        userSessions.put(userId, sessionId);
        log.info("User session registered: {} - {}", userId, sessionId);
    }

    public void unregisterUserSession(String userId) {
        userSessions.remove(userId);
        log.info("User session unregistered: {}", userId);
    }

    public boolean isUserOnline(String userId) {
        return userSessions.containsKey(userId);
    }
}
