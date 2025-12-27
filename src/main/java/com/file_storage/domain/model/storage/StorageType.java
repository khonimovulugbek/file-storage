package com.file_storage.domain.model.storage;

/**
 * Enum representing supported storage backend types
 */
public enum StorageType {
    MINIO("MinIO Object Storage"),
    S3("AWS S3"),
    SFTP("SFTP File Server");

    private final String description;

    StorageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
