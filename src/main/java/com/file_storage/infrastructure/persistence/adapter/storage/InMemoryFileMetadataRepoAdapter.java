package com.file_storage.infrastructure.persistence.adapter.storage;

import com.file_storage.application.port.out.storage.FileMetadataRepositoryPort;
import com.file_storage.domain.model.storage.FileAggregate;
import com.file_storage.domain.model.storage.FileId;
import com.file_storage.infrastructure.annotation.Adapter;

import java.util.HashMap;
import java.util.Map;

@Adapter
public class InMemoryFileMetadataRepoAdapter implements FileMetadataRepositoryPort {
    private final Map<String, FileAggregate> fileAggregates = new HashMap<>();

    @Override
    public FileAggregate save(FileAggregate fileAggregate) {
        fileAggregates.put(fileAggregate.fileId().value(), fileAggregate);
        return fileAggregate;
    }

    @Override
    public FileAggregate findById(FileId fileId) {
        return fileAggregates.get(fileId.value());
    }
}
