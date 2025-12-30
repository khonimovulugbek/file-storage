package com.file_storage.application.port.out.storage;

import com.file_storage.domain.model.storage.FileAggregate;
import com.file_storage.domain.model.storage.FileId;

public interface FileMetadataRepositoryPort {
    FileAggregate save(FileAggregate fileAggregate);

    FileAggregate findById(FileId fileId);
}
