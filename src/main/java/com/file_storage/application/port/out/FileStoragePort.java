package com.file_storage.application.port.out;

import java.io.InputStream;

public interface FileStoragePort {
    String uploadFile(InputStream inputStream, String fileName, String contentType, long size, String userId);
    InputStream downloadFile(String storageLocation);
    void deleteFile(String storageLocation);
    String getPresignedUrl(String storageLocation, int expirationSeconds);
}
