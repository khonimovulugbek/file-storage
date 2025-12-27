package com.file_storage.application.service.storage;

import com.file_storage.application.port.in.storage.*;
import com.file_storage.application.port.out.storage.*;
import com.file_storage.domain.model.storage.FileAggregate;
import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageReference;
import com.file_storage.domain.service.MetadataEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

/**
 * Application service implementing file download use case
 * Handles authorization, path decryption, and file retrieval
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileDownloadService implements DownloadFileUseCase {
    
    private final FileMetadataRepositoryPort metadataRepository;
    private final StorageNodeRegistryPort nodeRegistry;
    private final FileStoragePort fileStoragePort;
    private final MetadataEncryptionService encryptionService;
    
    @Override
    @Transactional(readOnly = true)
    public FileDownloadResult download(FileDownloadQuery query) {
        log.info("Starting file download: {} for user: {}", query.fileId(), query.userId());
        
        FileAggregate file = metadataRepository.findById(query.fileId())
            .orElseThrow(() -> new RuntimeException("File not found: " + query.fileId()));
        
        if (!file.isOwnedBy(query.userId())) {
            log.warn("Unauthorized download attempt: file {} by user {}", query.fileId(), query.userId());
            throw new RuntimeException("Unauthorized: User does not own this file");
        }
        
        ResolvedFileStorage resolved = validateAndResolveStorage(file);
        
        log.info("Resolved storage path for file: {} on node: {}", query.fileId(), resolved.node().nodeId());
        
        InputStream fileStream = fileStoragePort.retrieve(resolved.storageRef(), resolved.decryptedPath());
        
        return FileDownloadResult.builder()
            .fileStream(fileStream)
            .fileName(file.metadata().getFileName())
            .contentType(file.metadata().getContentType())
            .fileSize(file.metadata().getFileSize())
            .checksum(file.checksum().getHash())
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public String generateDownloadUrl(FileDownloadQuery query, int expirationSeconds) {
        log.info("Generating presigned URL: {} for user: {}", query.fileId(), query.userId());
        
        FileAggregate file = metadataRepository.findById(query.fileId())
            .orElseThrow(() -> new RuntimeException("File not found: " + query.fileId()));
        
        if (!file.isOwnedBy(query.userId())) {
            throw new RuntimeException("Unauthorized: User does not own this file");
        }
        
        ResolvedFileStorage resolved = validateAndResolveStorage(file);
        
        String presignedUrl = fileStoragePort.generatePresignedUrl(resolved.storageRef(), resolved.decryptedPath(), expirationSeconds);
        
        if (presignedUrl == null) {
            throw new RuntimeException("Presigned URLs not supported for storage type: " + resolved.storageRef().getStorageType());
        }
        
        log.info("Generated presigned URL for file: {} (expires in {} seconds)", query.fileId(), expirationSeconds);
        
        return presignedUrl;
    }
    
    private ResolvedFileStorage validateAndResolveStorage(FileAggregate file) {
        if (!file.canBeDownloaded()) {
            throw new RuntimeException("File is not available for download");
        }
        
        StorageNode node = nodeRegistry.findById(file.getStorageNodeId())
            .orElseThrow(() -> new RuntimeException("Storage node not found: " + file.getStorageNodeId()));
        
        if (node.status() != StorageNode.NodeStatus.ACTIVE) {
            log.error("Storage node is not active: {}", node.nodeId());
            throw new RuntimeException("Storage node is not available");
        }
        
        StorageReference storageRef = file.storageReference();
        String decryptedPath = encryptionService.decryptPath(
            storageRef.getEncryptedPath(),
            storageRef.getEncryptionKeyRef()
        );
        
        return new ResolvedFileStorage(node, storageRef, decryptedPath);
    }
    
    private record ResolvedFileStorage(StorageNode node, StorageReference storageRef, String decryptedPath) {}
}
