package com.file_storage.infrastructure.config;

import com.file_storage.application.port.out.storage.EncryptionPort;
import com.file_storage.domain.service.MetadataEncryptionService;
import com.file_storage.domain.service.StorageSelectionService;
import com.jcraft.jsch.JSch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for storage backends and domain services
 * Storage clients (MinIO, S3) are created dynamically per StorageNode
 */
@Configuration
public class StorageConfiguration {
    
    /**
     * JSch for SFTP connections
     */
    @Bean
    public JSch jsch() {
        return new JSch();
    }
    
    /**
     * Storage selection service (domain service)
     */
    @Bean
    public StorageSelectionService storageSelectionService() {
        return new StorageSelectionService();
    }
    
    /**
     * Metadata encryption service (domain service)
     */
    @Bean
    public MetadataEncryptionService metadataEncryptionService(EncryptionPort encryptionPort) {
        return new MetadataEncryptionService(encryptionPort);
    }
}
