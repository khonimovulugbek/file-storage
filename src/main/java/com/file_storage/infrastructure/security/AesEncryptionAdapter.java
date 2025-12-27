package com.file_storage.infrastructure.security;

import com.file_storage.application.port.out.storage.EncryptionPort;
import com.file_storage.domain.model.storage.EncryptedData;
import com.file_storage.domain.model.storage.EncryptionKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AES-256-GCM encryption adapter
 * In production, integrate with AWS KMS or HashiCorp Vault
 */
@Component
@Slf4j
public class AesEncryptionAdapter implements EncryptionPort {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;  // 96 bits for GCM
    private static final int TAG_SIZE = 128;  // 128 bits authentication tag
    
    // In-memory key vault (for demo - use real vault in production)
    private final Map<String, SecretKey> keyVault = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Override
    public EncryptedData encrypt(String plaintext, EncryptionKey key) {
        try {
            // Get secret key from vault
            SecretKey secretKey = getSecretKey(key.keyRef());
            
            // Generate random IV
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            
            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
            
            return new EncryptedData("AES-256-GCM", iv, ciphertext);
            
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    @Override
    public String decrypt(EncryptedData encryptedData, EncryptionKey key) {
        try {
            // Get secret key from vault
            SecretKey secretKey = getSecretKey(key.keyRef());
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, encryptedData.iv());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            
            // Decrypt
            byte[] plaintext = cipher.doFinal(encryptedData.ciphertext());
            
            return new String(plaintext);
            
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
    
    @Override
    public EncryptionKey generateKey() {
        try {
            // Generate AES key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE, secureRandom);
            SecretKey secretKey = keyGen.generateKey();
            
            // Generate key reference
            String keyRef = "key-" + UUID.randomUUID();
            String vaultId = "vault-default";
            
            // Store in vault
            keyVault.put(keyRef, secretKey);
            
            log.info("Generated new encryption key: {}", keyRef);
            
            return new EncryptionKey(keyRef, vaultId, EncryptionKey.EncryptionAlgorithm.AES_256_GCM);
            
        } catch (Exception e) {
            log.error("Key generation failed", e);
            throw new RuntimeException("Key generation failed", e);
        }
    }
    
    @Override
    public EncryptionKey getKey(String keyRef) {
        if (!keyVault.containsKey(keyRef)) {
            throw new RuntimeException("Encryption key not found: " + keyRef);
        }
        return new EncryptionKey(keyRef, "vault-default", EncryptionKey.EncryptionAlgorithm.AES_256_GCM);
    }
    
    private SecretKey getSecretKey(String keyRef) {
        SecretKey key = keyVault.get(keyRef);
        if (key == null) {
            throw new RuntimeException("Encryption key not found: " + keyRef);
        }
        return key;
    }
}
