package com.file_storage.application.service.storage;

import com.file_storage.application.port.in.storage.*;
import com.file_storage.application.port.out.storage.*;
import com.file_storage.domain.model.storage.*;
import com.file_storage.domain.service.MetadataEncryptionService;
import com.file_storage.domain.service.StorageSelectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Application service implementing file upload use case
 * Orchestrates domain services and infrastructure adapters
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService implements UploadFileUseCase {
    
    private final FileStoragePort fileStoragePort;
    private final FileMetadataRepositoryPort metadataRepository;
    private final StorageNodeRegistryPort nodeRegistry;
    private final StorageSelectionService storageSelectionService;
    private final MetadataEncryptionService encryptionService;
    
    @Override
    @Transactional
    public FileUploadResult upload(FileUploadCommand command) {
        log.info("Starting file upload: {} (size: {} bytes)", command.fileName(), command.fileSize());
        
        // 1. Calculate file checksum for deduplication
        FileChecksum checksum = calculateChecksum(command.fileContent(), command.fileSize());
        
        // 2. Check if file already exists (deduplication)
        Optional<FileAggregate> existingFile = metadataRepository.findByChecksum(checksum);
        if (existingFile.isPresent()) {
            log.info("File already exists (deduplicated): {}", checksum.getHash());
            return createUploadResult(existingFile.get(), true);
        }
        
        // 3. Select optimal storage node
        StorageType preferredType = parseStorageType(command.preferredStorageType());
        List<StorageNode> availableNodes = nodeRegistry.findAvailableNodes(preferredType);
        StorageNode selectedNode = storageSelectionService.selectNode(availableNodes, preferredType);
        
        log.info("Selected storage node: {} (type: {})", selectedNode.nodeId(), selectedNode.storageType());
        
        // 4. Upload file to storage backend
        StorageContext context = StorageContext.builder()
            .fileName(command.fileName())
            .contentType(command.contentType())
            .fileSize(command.fileSize())
            .bucket(generateBucketName())
            .targetNode(selectedNode)
            .basePath(generateBasePath(command.ownerId()))
            .build();
        
        StorageResult storageResult = fileStoragePort.store(command.fileContent(), context);
        
        // 5. Encrypt storage path
        MetadataEncryptionService.EncryptedPathResult encryptedPath = 
            encryptionService.encryptPath(storageResult.absolutePath());
        
        // 6. Create storage reference (encrypted)
        StorageReference storageReference = StorageReference.builder()
            .storageType(selectedNode.storageType())
            .storageNodeId(selectedNode.nodeId())
            .encryptedPath(encryptedPath.encryptedPath())
            .encryptionKeyRef(encryptedPath.keyRef())
            .bucket(storageResult.bucket())
            .region(storageResult.region())
            .build();
        
        // 7. Create file metadata
        FileMetadata metadata = FileMetadata.builder()
            .fileName(command.fileName())
            .contentType(command.contentType())
            .fileSize(command.fileSize())
            .ownerId(command.ownerId())
            .uploadedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .status(FileMetadata.FileStatus.ACTIVE)
            .build();
        
        // 8. Create file aggregate
        FileAggregate fileAggregate = FileAggregate.builder()
            .fileId(FileId.generate())
            .metadata(metadata)
            .storageReference(storageReference)
            .checksum(checksum)
            .build();
        
        // 9. Persist to database
        FileAggregate savedFile = metadataRepository.save(fileAggregate);
        
        // 10. Update node capacity
        nodeRegistry.updateNodeCapacity(
            selectedNode.nodeId(),
            selectedNode.usedCapacityGb() + (command.fileSize() / (1024 * 1024 * 1024)),
            selectedNode.fileCount() + 1
        );
        
        log.info("File uploaded successfully: {} to node {}", savedFile.getFileId(), selectedNode.nodeId());
        
        return createUploadResult(savedFile, false);
    }
    
    private FileChecksum calculateChecksum(InputStream content, long fileSize) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = content.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return new FileChecksum(FileChecksum.ChecksumAlgorithm.SHA256, hexString.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }
    
    private StorageType parseStorageType(String preferredType) {
        if (preferredType == null || preferredType.isBlank()) {
            return null;
        }
        try {
            return StorageType.valueOf(preferredType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid storage type: {}, using default", preferredType);
            return null;
        }
    }
    
    private String generateBucketName() {
        // Generate bucket name based on date (e.g., files-2024-12)
        return "files-" + LocalDateTime.now().getYear() + "-" + 
               String.format("%02d", LocalDateTime.now().getMonthValue());
    }
    
    private String generateBasePath(java.util.UUID ownerId) {
        // Organize files by user ID
        return "users/" + ownerId.toString();
    }
    
    private FileUploadResult createUploadResult(FileAggregate file, boolean deduplicated) {
        return FileUploadResult.builder()
            .fileId(file.getFileId())
            .fileName(file.getMetadata().getFileName())
            .fileSize(file.getMetadata().getFileSize())
            .contentType(file.getMetadata().getContentType())
            .checksum(file.getChecksum().getHash())
            .storageNodeId(file.getStorageNodeId())
            .uploadedAt(file.getMetadata().getUploadedAt())
            .deduplicated(deduplicated)
            .build();
    }
}
