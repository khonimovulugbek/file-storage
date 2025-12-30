package com.file_storage.domain.exception;

public class StorageNodeNotFoundException extends DomainException {
    public StorageNodeNotFoundException(String nodeId) {
        super("Storage node not found with id: " + nodeId);
    }
}
