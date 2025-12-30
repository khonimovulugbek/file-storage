package com.file_storage.domain.exception;

import com.file_storage.domain.model.storage.FileId;

public class FileNotFoundException extends DomainException {
    public FileNotFoundException(FileId fileId) {
        super("File not found with id: " + fileId.value());
    }
}
