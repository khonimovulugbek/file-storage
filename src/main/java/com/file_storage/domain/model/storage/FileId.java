package com.file_storage.domain.model.storage;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * Value Object representing unique file identifier
 */
@Getter
@EqualsAndHashCode
public class FileId {
    private final UUID value;

    private FileId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("FileId cannot be null");
        }
        this.value = value;
    }

    public static FileId of(UUID value) {
        return new FileId(value);
    }

    public static FileId generate() {
        return new FileId(UUID.randomUUID());
    }

    public static FileId fromString(String value) {
        return new FileId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
