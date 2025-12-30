package com.file_storage.domain.model.storage;

public record OwnerId(String value) {
    public OwnerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OwnerId cannot be null or empty");
        }
    }
}
