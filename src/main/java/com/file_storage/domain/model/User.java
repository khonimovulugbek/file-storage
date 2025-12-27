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
public class User {
    private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }

    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }
}
