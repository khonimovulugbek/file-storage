package com.file_storage.domain.service;

import com.file_storage.application.port.out.storage.EncryptionPort;
import com.file_storage.domain.model.storage.EncryptedData;
import com.file_storage.domain.model.storage.EncryptionKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for encrypting/decrypting storage node credentials
 * Uses a dedicated encryption key for credential protection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialEncryptionService {
    
    private final EncryptionPort encryptionPort;
    
    // Key reference for credential encryption - stored in encryption_keys table
    private static final String CREDENTIAL_KEY_REF = "key-credentials-master";
    
    /**
     * Encrypt credential (access key or secret key)
     * @param plainCredential Plain text credential
     * @return Serialized encrypted credential
     */
    public String encryptCredential(String plainCredential) {
        if (plainCredential == null || plainCredential.isBlank()) {
            return null;
        }
        
        try {
            EncryptionKey key = getOrCreateCredentialKey();
            EncryptedData encryptedData = encryptionPort.encrypt(plainCredential, key);
            String serialized = encryptedData.serialize();
            
            log.debug("Credential encrypted successfully");
            return serialized;
            
        } catch (Exception e) {
            log.error("Failed to encrypt credential", e);
            throw new RuntimeException("Credential encryption failed", e);
        }
    }
    
    /**
     * Decrypt credential
     * @param encryptedCredential Serialized encrypted credential
     * @return Plain text credential
     */
    public String decryptCredential(String encryptedCredential) {
        if (encryptedCredential == null || encryptedCredential.isBlank()) {
            return null;
        }
        
        try {
            EncryptionKey key = getOrCreateCredentialKey();
            EncryptedData encryptedData = EncryptedData.deserialize(encryptedCredential);
            String plainCredential = encryptionPort.decrypt(encryptedData, key);
            
            log.debug("Credential decrypted successfully");
            return plainCredential;
            
        } catch (Exception e) {
            log.error("Failed to decrypt credential", e);
            throw new RuntimeException("Credential decryption failed", e);
        }
    }
    
    /**
     * Get or create the master credential encryption key
     * The key is automatically persisted by DatabaseEncryptionAdapter
     */
    private EncryptionKey getOrCreateCredentialKey() {
        try {
            // Try to get existing key
            return encryptionPort.getKey(CREDENTIAL_KEY_REF);
        } catch (RuntimeException e) {
            // Key doesn't exist, generate a new one
            // The generated key will be persisted automatically
            log.info("Credential encryption key not found, generating new key");
            return encryptionPort.generateKey();
        }
    }
}
