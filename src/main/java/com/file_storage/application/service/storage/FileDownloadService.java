package com.file_storage.application.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.file_storage.application.port.in.storage.DownloadFileUseCase;
import com.file_storage.application.port.out.storage.FileMetadataRepositoryPort;
import com.file_storage.application.port.out.storage.FileStoragePort;
import com.file_storage.domain.event.FileDownloadedEvent;
import com.file_storage.domain.exception.FileNotFoundException;
import com.file_storage.domain.model.storage.FileAggregate;

import java.io.InputStream;

@Service
@Slf4j
public class FileDownloadService implements DownloadFileUseCase {
    private final FileMetadataRepositoryPort fileMetadataRepository;
    private final FileStoragePort fileStoragePort;

    public FileDownloadService(FileMetadataRepositoryPort fileMetadataRepository, FileStoragePort fileStoragePort) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    public FileDownloadResult download(FileDownloadQuery query) {
        log.info("Downloading file: {}", query.fileId().value());
        
        FileAggregate file = fileMetadataRepository.findById(query.fileId());
        if (file == null) {
            throw new FileNotFoundException(query.fileId());
        }
        
        file.validateForDownload();
        
        InputStream fileStream = fileStoragePort.retrieve(file.storageReference());
        
        FileDownloadedEvent event = FileDownloadedEvent.of(file.fileId(), file.fileMetadata().fileName());
        log.info("File downloaded successfully: {} - Event: {}", file.fileId(), event);
        
        return FileDownloadResult.builder()
                .fileStream(fileStream)
                .fileName(file.fileMetadata().fileName())
                .contentType(file.fileMetadata().contentType())
                .fileSize(file.fileMetadata().fileSize())
                .checksum(file.checksum().hash())
                .build();
    }
}
