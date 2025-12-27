package com.file_storage.domain.service;

import com.file_storage.application.port.out.storage.EncryptionPort;
import com.file_storage.domain.model.storage.EncryptedData;
import com.file_storage.domain.model.storage.EncryptionKey;

/**
 * Domain service for encrypting/decrypting file metadata
 * Ensures storage paths are never stored in plaintext
 */
public class MetadataEncryptionService {
    
    private final EncryptionPort encryptionPort;
    
    public MetadataEncryptionService(EncryptionPort encryptionPort) {
        this.encryptionPort = encryptionPort;
    }
    
    /**
     * Encrypt storage path before persisting to database
     * @param plainPath Raw storage path
     * @return Encrypted path and key reference
     */
    public EncryptedPathResult encryptPath(String plainPath) {
        // Generate new encryption key for this file
        EncryptionKey key = encryptionPort.generateKey();
        
        // Encrypt the path
        EncryptedData encryptedData = encryptionPort.encrypt(plainPath, key);
        
        // Return serialized encrypted data and key reference
        return new EncryptedPathResult(
            encryptedData.serialize(),
            key.keyRef()
        );
    }
    
    /**
     * Decrypt storage path for file retrieval
     * @param encryptedPath Encrypted path from database
     * @param keyRef Key reference
     * @return Decrypted plaintext path
     */
    public String decryptPath(String encryptedPath, String keyRef) {
        // Retrieve encryption key from vault
        EncryptionKey key = encryptionPort.getKey(keyRef);
        
        // Deserialize encrypted data
        EncryptedData encryptedData = EncryptedData.deserialize(encryptedPath);
        
        // Decrypt and return path
        return encryptionPort.decrypt(encryptedData, key);
    }
    
    /**
     * Result object containing encrypted path and key reference
     */
    public record EncryptedPathResult(String encryptedPath, String keyRef) {}
}
