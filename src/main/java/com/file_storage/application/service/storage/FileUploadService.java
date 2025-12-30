package com.file_storage.application.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.file_storage.application.port.in.storage.UploadFileUseCase;
import com.file_storage.application.port.out.storage.*;
import com.file_storage.domain.event.FileUploadedEvent;
import com.file_storage.domain.model.storage.*;
import com.file_storage.domain.service.FileChecksumService;
import com.file_storage.domain.service.FilePathGenerator;

import java.io.BufferedInputStream;
import java.time.LocalDateTime;

@Service
@Slf4j
public class FileUploadService implements UploadFileUseCase {
    private final StorageNodeRegistryPort nodeRegistry;
    private final FileStoragePort fileStoragePort;
    private final FileMetadataRepositoryPort metadataRepository;
    private final FileChecksumService checksumService;
    private final FilePathGenerator pathGenerator;

    public FileUploadService(
            StorageNodeRegistryPort nodeRegistry,
            FileStoragePort fileStoragePort,
            FileMetadataRepositoryPort metadataRepository,
            FileChecksumService checksumService,
            FilePathGenerator pathGenerator) {
        this.nodeRegistry = nodeRegistry;
        this.fileStoragePort = fileStoragePort;
        this.metadataRepository = metadataRepository;
        this.checksumService = checksumService;
        this.pathGenerator = pathGenerator;
    }

    @Override
    public FileUploadResult upload(FileUploadCommand command) {
        log.info("Starting file upload: {} (size: {} bytes)", command.fileName(), command.fileSize());
        
        BufferedInputStream bufferedContent = new BufferedInputStream(command.content());
        bufferedContent.mark(Integer.MAX_VALUE);
        FileChecksum checksum = checksumService.calculateChecksum(bufferedContent, FileChecksum.ChecksumAlgorithm.SHA256);

        try {
            bufferedContent.reset();
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset input stream after checksum calculation", e);
        }

        StorageNode selectedNode = nodeRegistry.findRandomAvailableNode();
        log.info("Selected storage node: {} (type: {})", selectedNode.nodeId(), selectedNode.storageType());

        UserId<?> ownerId = command.ownerId() != null ? new UserId<>(command.ownerId()) : null;
        String basePath = pathGenerator.generateBasePath(ownerId);

        StorageContext context = StorageContext.builder()
                .fileName(command.fileName())
                .contentType(command.contentType())
                .fileSize(command.fileSize())
                .bucket(selectedNode.bucket())
                .storageNode(selectedNode)
                .basePath(basePath)
                .build();

        StorageResult storageResult = fileStoragePort.store(bufferedContent, context);

        StorageReference storageReference = StorageReference.builder()
                .storageType(selectedNode.storageType())
                .storageNodeId(selectedNode.nodeId())
                .absolutePath(storageResult.absolutePath())
                .path(storageResult.path())
                .bucket(storageResult.bucket())
                .region(storageResult.region())
                .build();

        FileMetadata metadata = FileMetadata.builder()
                .fileName(command.fileName())
                .contentType(command.contentType())
                .fileSize(command.fileSize())
                .status(FileMetadata.FileStatusEnum.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        FileAggregate fileAggregate = FileAggregate.builder()
                .fileId(FileId.generate())
                .fileMetadata(metadata)
                .storageReference(storageReference)
                .checksum(checksum)
                .build();

        FileAggregate savedFile = metadataRepository.save(fileAggregate);

        FileUploadedEvent event = FileUploadedEvent.of(
                savedFile.fileId(),
                savedFile.fileMetadata().fileName(),
                savedFile.fileMetadata().fileSize(),
                selectedNode.nodeId()
        );
        log.info("File uploaded successfully: {} to node {} - Event: {}", savedFile.fileId(), selectedNode.nodeId(), event);

        return createUploadResult(savedFile);
    }


    private FileUploadResult createUploadResult(FileAggregate file) {
        return FileUploadResult.builder()
                .fileId(file.fileId())
                .fileName(file.fileMetadata().fileName())
                .fileSize(file.fileMetadata().fileSize())
                .contentType(file.fileMetadata().contentType())
                .checksum(file.checksum().hash())
                .storageNodeId(file.storageNodeId())
                .absolutePath(file.storageReference().absolutePath())
                .createdAt(file.fileMetadata().createdAt())
                .updatedAt(file.fileMetadata().updatedAt())
                .build();
    }
}
