package com.file_storage.domain.model.storage;

import java.util.UUID;

public record FileId(String value) {
    public FileId {
        if (value == null) {
            throw new IllegalArgumentException("FileId cannot be null");
        }
    }

    public static FileId generate() {
        return new FileId(UUID.randomUUID().toString());
    }
}
