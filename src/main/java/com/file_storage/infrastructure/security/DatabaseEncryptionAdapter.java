package com.file_storage.infrastructure.security;

import com.file_storage.application.port.out.storage.EncryptionPort;
import com.file_storage.domain.model.storage.EncryptedData;
import com.file_storage.domain.model.storage.EncryptionKey;
import com.file_storage.infrastructure.persistence.entity.storage.EncryptionKeyEntity;
import com.file_storage.infrastructure.persistence.repository.storage.EncryptionKeyJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Database-backed encryption adapter for multi-instance deployments
 * Stores encryption keys in PostgreSQL instead of in-memory
 * 
 * PRODUCTION: Replace with AWS KMS, HashiCorp Vault, or Azure Key Vault
 */
@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class DatabaseEncryptionAdapter implements EncryptionPort {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;
    
    private final EncryptionKeyJpaRepository keyRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Value("${encryption.master-key:CHANGE_THIS_IN_PRODUCTION_32_CHARS}")
    private String masterKeyBase64;
    
    @Override
    public EncryptedData encrypt(String plaintext, EncryptionKey key) {
        try {
            SecretKey secretKey = getSecretKey(key.keyRef());
            
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            
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
            SecretKey secretKey = getSecretKey(key.keyRef());
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, encryptedData.iv());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            
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
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE, secureRandom);
            SecretKey secretKey = keyGen.generateKey();
            
            String keyRef = "key-" + UUID.randomUUID();
            String vaultId = "vault-database";
            
            String encryptedKey = encryptKeyWithMasterKey(secretKey);
            
            EncryptionKeyEntity entity = EncryptionKeyEntity.builder()
                .keyRef(keyRef)
                .encryptedKey(encryptedKey)
                .algorithm("AES-256-GCM")
                .vaultId(vaultId)
                .createdAt(LocalDateTime.now())
                .status("ACTIVE")
                .build();
            
            keyRepository.save(entity);
            
            log.info("Generated and stored new encryption key: {}", keyRef);
            
            return new EncryptionKey(keyRef, vaultId, EncryptionKey.EncryptionAlgorithm.AES_256_GCM);
            
        } catch (Exception e) {
            log.error("Key generation failed", e);
            throw new RuntimeException("Key generation failed", e);
        }
    }
    
    @Override
    public EncryptionKey getKey(String keyRef) {
        EncryptionKeyEntity entity = keyRepository.findById(keyRef)
            .orElseThrow(() -> new RuntimeException("Encryption key not found: " + keyRef));
        
        return new EncryptionKey(
            entity.getKeyRef(), 
            entity.getVaultId(), 
            EncryptionKey.EncryptionAlgorithm.AES_256_GCM
        );
    }
    
    private SecretKey getSecretKey(String keyRef) {
        EncryptionKeyEntity entity = keyRepository.findById(keyRef)
            .orElseThrow(() -> new RuntimeException("Encryption key not found: " + keyRef));
        
        return decryptKeyWithMasterKey(entity.getEncryptedKey());
    }
    
    private String encryptKeyWithMasterKey(SecretKey key) throws Exception {
        SecretKey masterKey = getMasterKey();
        
        byte[] iv = new byte[IV_SIZE];
        secureRandom.nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, gcmSpec);
        
        byte[] encryptedKey = cipher.doFinal(key.getEncoded());
        
        byte[] combined = new byte[iv.length + encryptedKey.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedKey, 0, combined, iv.length, encryptedKey.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }
    
    private SecretKey decryptKeyWithMasterKey(String encryptedKeyBase64) {
        try {
            SecretKey masterKey = getMasterKey();
            
            byte[] combined = Base64.getDecoder().decode(encryptedKeyBase64);
            
            byte[] iv = new byte[IV_SIZE];
            byte[] encryptedKey = new byte[combined.length - IV_SIZE];
            
            System.arraycopy(combined, 0, iv, 0, IV_SIZE);
            System.arraycopy(combined, IV_SIZE, encryptedKey, 0, encryptedKey.length);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, gcmSpec);
            
            byte[] keyBytes = cipher.doFinal(encryptedKey);
            
            return new SecretKeySpec(keyBytes, "AES");
            
        } catch (Exception e) {
            log.error("Failed to decrypt key with master key", e);
            throw new RuntimeException("Failed to decrypt key", e);
        }
    }
    
    private SecretKey getMasterKey() {
        byte[] keyBytes = Base64.getDecoder().decode(masterKeyBase64);
        return new SecretKeySpec(keyBytes, "AES");
    }
}
