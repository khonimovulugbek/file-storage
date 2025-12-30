package com.file_storage.domain.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredOn();
}
