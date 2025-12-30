package com.file_storage.application.port.out.storage;

import com.file_storage.domain.model.storage.StorageReference;

import java.io.InputStream;

public interface FileStoragePort {
    StorageResult store(InputStream content, StorageContext context);

    InputStream retrieve(StorageReference storageReference);
}
