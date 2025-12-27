package com.file_storage.application.port.out.storage;

import com.file_storage.domain.model.storage.EncryptedData;
import com.file_storage.domain.model.storage.EncryptionKey;

/**
 * Outbound port for encryption/decryption operations
 * Implemented by encryption adapters (AES, AWS KMS, etc.)
 */
public interface EncryptionPort {
    
    /**
     * Encrypt plaintext using provided key
     * @param plaintext Text to encrypt
     * @param key Encryption key
     * @return Encrypted data with IV and ciphertext
     */
    EncryptedData encrypt(String plaintext, EncryptionKey key);
    
    /**
     * Decrypt ciphertext using provided key
     * @param encryptedData Encrypted data with IV
     * @param key Encryption key
     * @return Decrypted plaintext
     */
    String decrypt(EncryptedData encryptedData, EncryptionKey key);
    
    /**
     * Generate new encryption key
     * @return New encryption key reference
     */
    EncryptionKey generateKey();
    
    /**
     * Retrieve encryption key from vault
     * @param keyRef Key reference
     * @return Encryption key
     */
    EncryptionKey getKey(String keyRef);
}
