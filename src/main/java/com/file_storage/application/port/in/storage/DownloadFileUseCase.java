package com.file_storage.application.port.in.storage;

/**
 * Inbound port for file download use case
 * Defines the contract for downloading files
 */
public interface DownloadFileUseCase {
    
    /**
     * Download file from storage system
     * @param query Download query with file ID and user ID
     * @return Download result with file stream and metadata
     */
    FileDownloadResult download(FileDownloadQuery query);
    
    /**
     * Generate presigned download URL (for S3/MinIO)
     * @param query Download query
     * @param expirationSeconds URL expiration time
     * @return Presigned URL
     */
    String generateDownloadUrl(FileDownloadQuery query, int expirationSeconds);
}
