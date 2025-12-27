package com.file_storage.application.port.in;

import com.file_storage.domain.model.File;
import com.file_storage.domain.model.FileUploadRequest;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface FileUseCase {
    File uploadFile(FileUploadRequest request, UUID userId, UUID folderId);
    InputStream downloadFile(UUID fileId, UUID userId);
    File getFileMetadata(UUID fileId, UUID userId);
    List<File> listUserFiles(UUID userId);
    List<File> listFolderFiles(UUID folderId, UUID userId);
    void deleteFile(UUID fileId, UUID userId);
    String getDownloadUrl(UUID fileId, UUID userId);
    List<File> searchFiles(String query, UUID userId);
}
