package com.file_storage.infrastructure.storage.adapter;

import com.file_storage.application.port.out.storage.FileStoragePort;
import com.file_storage.application.port.out.storage.StorageContext;
import com.file_storage.application.port.out.storage.StorageResult;
import com.file_storage.domain.model.storage.StorageReference;
import com.file_storage.domain.model.storage.StorageType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Router that delegates to appropriate storage adapter based on storage type
 * Implements Strategy Pattern for pluggable storage backends
 */
@Component
@Primary
@RequiredArgsConstructor
public class FileStoragePortRouter implements FileStoragePort {
    
    private final MinIOStorageAdapter minioAdapter;
    private final S3StorageAdapter s3Adapter;
    private final SFTPStorageAdapter sftpAdapter;
    
    @Override
    public StorageResult store(InputStream content, StorageContext context) {
        FileStoragePort adapter = getAdapter(context.targetNode().storageType());
        return adapter.store(content, context);
    }
    
    @Override
    public InputStream retrieve(StorageReference reference, String decryptedPath) {
        FileStoragePort adapter = getAdapter(reference.getStorageType());
        return adapter.retrieve(reference, decryptedPath);
    }
    
    @Override
    public void delete(StorageReference reference, String decryptedPath) {
        FileStoragePort adapter = getAdapter(reference.getStorageType());
        adapter.delete(reference, decryptedPath);
    }
    
    @Override
    public boolean exists(StorageReference reference, String decryptedPath) {
        FileStoragePort adapter = getAdapter(reference.getStorageType());
        return adapter.exists(reference, decryptedPath);
    }
    
    @Override
    public String generatePresignedUrl(StorageReference reference, String decryptedPath, int expirationSeconds) {
        FileStoragePort adapter = getAdapter(reference.getStorageType());
        return adapter.generatePresignedUrl(reference, decryptedPath, expirationSeconds);
    }
    
    private FileStoragePort getAdapter(StorageType storageType) {
        return switch (storageType) {
            case MINIO -> minioAdapter;
            case S3 -> s3Adapter;
            case SFTP -> sftpAdapter;
        };
    }
}
