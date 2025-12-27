package com.file_storage.application.service;

import com.file_storage.application.port.in.FileUseCase;
import com.file_storage.application.port.out.CachePort;
import com.file_storage.application.port.out.FilePort;
import com.file_storage.application.port.out.FileStoragePort;
import com.file_storage.domain.model.File;
import com.file_storage.domain.model.FileUploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService implements FileUseCase {

    private final FilePort filePort;
    private final FileStoragePort fileStoragePort;
    private final CachePort cachePort;

    @Override
    @Transactional
    public File uploadFile(FileUploadRequest request, UUID userId, UUID folderId) {
        try {
            byte[] fileBytes = request.getInputStream().readAllBytes();
            String checksum = calculateChecksum(fileBytes);
            
            String storageLocation = fileStoragePort.uploadFile(
                request.getInputStream(), 
                request.getFileName(), 
                request.getContentType(), 
                request.getSize(), 
                userId.toString()
            );

            File file = File.builder()
                    .id(UUID.randomUUID())
                    .name(request.getFileName())
                    .size(request.getSize())
                    .contentType(request.getContentType())
                    .checksum(checksum)
                    .status(File.FileStatus.ACTIVE)
                    .ownerId(userId)
                    .parentFolderId(folderId)
                    .storageLocation(storageLocation)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            File saved = filePort.save(file);
            log.info("File uploaded successfully: {}", saved.getId());

            cachePort.deletePattern("files:user:" + userId + ":*");

            return saved;
        } catch (Exception e) {
            log.error("Error uploading file", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public InputStream downloadFile(UUID fileId, UUID userId) {
        File file = filePort.findById(fileId, userId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return fileStoragePort.downloadFile(file.getStorageLocation());
    }

    @Override
    public File getFileMetadata(UUID fileId, UUID userId) {
        String cacheKey = "file:metadata:" + fileId;
        Object cached = cachePort.get(cacheKey);

        if (cached instanceof File) {
            return (File) cached;
        }

        File file = filePort.findById(fileId, userId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        cachePort.set(cacheKey, file, Duration.ofHours(1));

        return file;
    }

    @Override
    public List<File> listUserFiles(UUID userId) {
        String cacheKey = "files:user:" + userId;
        Object cached = cachePort.get(cacheKey);

        if (cached instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<File> cachedFiles = (List<File>) cached;
            return cachedFiles;
        }

        List<File> files = filePort.findActiveFilesByOwner(userId);

        cachePort.set(cacheKey, files, Duration.ofMinutes(15));

        return files;
    }

    @Override
    public List<File> listFolderFiles(UUID folderId, UUID userId) {
        return filePort.findByParentFolderId(folderId)
                .stream()
                .filter(f -> f.getOwnerId().equals(userId))
                .toList();
    }

    @Override
    @Transactional
    public void deleteFile(UUID fileId, UUID userId) {
        File file = filePort.findById(fileId, userId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        fileStoragePort.deleteFile(file.getStorageLocation());
        filePort.delete(fileId);

        cachePort.delete("file:metadata:" + fileId);
        cachePort.deletePattern("files:user:" + userId + ":*");

        log.info("File deleted successfully: {}", fileId);
    }

    @Override
    public String getDownloadUrl(UUID fileId, UUID userId) {
        File file = filePort.findById(fileId, userId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return fileStoragePort.getPresignedUrl(file.getStorageLocation(), 3600);
    }

    @Override
    public List<File> searchFiles(String query, UUID userId) {
        return filePort.searchByName(userId, query);
    }

    private String calculateChecksum(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
