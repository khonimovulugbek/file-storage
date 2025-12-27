package com.file_storage.application.port.in.storage;

import lombok.Builder;

import java.io.InputStream;

/**
 * Result object returned for file download
 */
@Builder
public record FileDownloadResult(InputStream fileStream, String fileName, String contentType, long fileSize,
                                 String checksum) {
    public FileDownloadResult {
        if (fileStream == null) {
            throw new IllegalArgumentException("File stream cannot be null");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

    }
}
