package com.file_storage.application.port.in.storage;

/**
 * Inbound port for file upload use case
 * Defines the contract for uploading files
 */
public interface UploadFileUseCase {
    
    /**
     * Upload file to storage system
     * @param command Upload command with file data
     * @return Upload result with file ID and metadata
     */
    FileUploadResult upload(FileUploadCommand command);
}
