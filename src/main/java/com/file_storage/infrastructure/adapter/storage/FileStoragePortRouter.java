package com.file_storage.infrastructure.adapter.storage;

import org.springframework.context.annotation.Primary;
import com.file_storage.application.port.out.storage.FileStoragePort;
import com.file_storage.application.port.out.storage.StorageContext;
import com.file_storage.application.port.out.storage.StorageResult;
import com.file_storage.domain.model.storage.StorageReference;
import com.file_storage.domain.model.storage.StorageType;
import com.file_storage.infrastructure.annotation.Adapter;

import java.io.InputStream;

@Adapter
@Primary
public class FileStoragePortRouter implements FileStoragePort {
    private final MinIOStorageAdapter minioStorageAdapter;
    private final S3StorageAdapter s3StorageAdapter;
    private final SFTPStorageAdapter sftpStorageAdapter;

    public FileStoragePortRouter(MinIOStorageAdapter minioStorageAdapter, S3StorageAdapter s3StorageAdapter, SFTPStorageAdapter sftpStorageAdapter) {
        this.minioStorageAdapter = minioStorageAdapter;
        this.s3StorageAdapter = s3StorageAdapter;
        this.sftpStorageAdapter = sftpStorageAdapter;
    }

    @Override
    public StorageResult store(InputStream content, StorageContext context) {
        FileStoragePort adapter = getAdapter(context.storageNode().storageType());
        return adapter.store(content, context);
    }

    @Override
    public InputStream retrieve(StorageReference reference) {
        FileStoragePort adapter = getAdapter(reference.storageType());
        return adapter.retrieve(reference);
    }

    private FileStoragePort getAdapter(StorageType storageType) {
        return switch (storageType) {
            case MINIO -> minioStorageAdapter;
            case S3 -> s3StorageAdapter;
            case SFTP -> sftpStorageAdapter;
        };
    }
}
