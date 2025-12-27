package com.file_storage.application.port.out.storage;

import com.file_storage.domain.model.storage.StorageReference;

import java.io.InputStream;

/**
 * Outbound port for file storage operations
 * Implemented by storage adapters (MinIO, S3, SFTP)
 */
public interface FileStoragePort {
    
    /**
     * Store file content to storage backend
     * @param content File input stream
     * @param context Storage context with metadata
     * @return Storage result with absolute path and metadata
     */
    StorageResult store(InputStream content, StorageContext context);
    
    /**
     * Retrieve file content from storage backend
     * @param reference Storage reference with encrypted path
     * @param decryptedPath Decrypted absolute path
     * @return File input stream
     */
    InputStream retrieve(StorageReference reference, String decryptedPath);
    
    /**
     * Delete file from storage backend
     * @param reference Storage reference
     * @param decryptedPath Decrypted absolute path
     */
    void delete(StorageReference reference, String decryptedPath);
    
    /**
     * Check if file exists in storage
     * @param reference Storage reference
     * @param decryptedPath Decrypted absolute path
     * @return true if file exists
     */
    boolean exists(StorageReference reference, String decryptedPath);
    
    /**
     * Generate presigned download URL (for S3/MinIO)
     * @param reference Storage reference
     * @param decryptedPath Decrypted absolute path
     * @param expirationSeconds URL expiration time
     * @return Presigned URL or null if not supported
     */
    String generatePresignedUrl(StorageReference reference, String decryptedPath, int expirationSeconds);
}
