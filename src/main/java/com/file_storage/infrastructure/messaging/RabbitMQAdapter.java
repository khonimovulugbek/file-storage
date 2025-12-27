package com.file_storage.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.file_storage.application.port.out.MessageQueuePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQAdapter implements MessageQueuePort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final String FILE_EVENTS_EXCHANGE = "file.events";
    private static final String SYNC_EVENTS_EXCHANGE = "sync.events";
    private static final String NOTIFICATION_EXCHANGE = "notification.events";
    private static final String VIRUS_SCAN_EXCHANGE = "virus.scan";

    @Override
    public void publishFileUploadedEvent(String fileId, String userId) {
        try {
            Map<String, String> event = new HashMap<>();
            event.put("eventType", "FILE_UPLOADED");
            event.put("fileId", fileId);
            event.put("userId", userId);
            event.put("timestamp", String.valueOf(System.currentTimeMillis()));

            rabbitTemplate.convertAndSend(FILE_EVENTS_EXCHANGE, "file.uploaded", event);
            log.info("Published file uploaded event: {}", fileId);
        } catch (Exception e) {
            log.error("Failed to publish file uploaded event", e);
        }
    }

    @Override
    public void publishFileDeletedEvent(String fileId, String userId) {
        try {
            Map<String, String> event = new HashMap<>();
            event.put("eventType", "FILE_DELETED");
            event.put("fileId", fileId);
            event.put("userId", userId);
            event.put("timestamp", String.valueOf(System.currentTimeMillis()));

            rabbitTemplate.convertAndSend(FILE_EVENTS_EXCHANGE, "file.deleted", event);
            log.info("Published file deleted event: {}", fileId);
        } catch (Exception e) {
            log.error("Failed to publish file deleted event", e);
        }
    }

    @Override
    public void publishSyncEvent(String eventType, String payload) {
        try {
            Map<String, String> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("payload", payload);
            event.put("timestamp", String.valueOf(System.currentTimeMillis()));

            rabbitTemplate.convertAndSend(SYNC_EVENTS_EXCHANGE, "sync.event", event);
            log.debug("Published sync event: {}", eventType);
        } catch (Exception e) {
            log.error("Failed to publish sync event", e);
        }
    }

    @Override
    public void publishNotificationEvent(String userId, String message) {
        try {
            Map<String, String> event = new HashMap<>();
            event.put("userId", userId);
            event.put("message", message);
            event.put("timestamp", String.valueOf(System.currentTimeMillis()));

            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.send", event);
            log.info("Published notification event for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to publish notification event", e);
        }
    }

    @Override
    public void publishVirusScanRequest(String fileId, String storageLocation) {
        try {
            Map<String, String> event = new HashMap<>();
            event.put("fileId", fileId);
            event.put("storageLocation", storageLocation);
            event.put("timestamp", String.valueOf(System.currentTimeMillis()));

            rabbitTemplate.convertAndSend(VIRUS_SCAN_EXCHANGE, "scan.request", event);
            log.info("Published virus scan request for file: {}", fileId);
        } catch (Exception e) {
            log.error("Failed to publish virus scan request", e);
        }
    }
}
