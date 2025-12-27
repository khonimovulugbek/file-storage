package com.file_storage.infrastructure.security;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating encryption keys
 * Run this class to generate a secure master key for production
 */
@Slf4j
public class EncryptionKeyGenerator {
    
    private static final int KEY_SIZE_BYTES = 32; // 256 bits
    
    public static void main(String[] args) {
        log.info("=== Encryption Key Generator ===");
        log.info("Generating secure AES-256 master key...\n");
        
        String masterKey = generateMasterKey();
        
        System.out.println("Generated Master Key (Base64):");
        System.out.println(masterKey);
        System.out.println();
        System.out.println("Add this to your application.yaml:");
        System.out.println("encryption:");
        System.out.println("  master-key: \"" + masterKey + "\"");
        System.out.println();
        System.out.println("Or set as environment variable:");
        System.out.println("export ENCRYPTION_MASTER_KEY=\"" + masterKey + "\"");
        System.out.println();
        System.out.println("IMPORTANT SECURITY NOTES:");
        System.out.println("1. Never commit this key to version control");
        System.out.println("2. Store in a secure secrets management service (AWS Secrets Manager, HashiCorp Vault, etc.)");
        System.out.println("3. Rotate the key periodically");
        System.out.println("4. Use different keys for different environments (dev, staging, prod)");
        System.out.println("5. Backup the key securely - losing it means losing access to encrypted data");
    }
    
    /**
     * Generate a cryptographically secure random key
     * @return Base64-encoded 256-bit key
     */
    public static String generateMasterKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[KEY_SIZE_BYTES];
        secureRandom.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }
    
    /**
     * Validate a master key
     * @param masterKey Base64-encoded key
     * @return true if valid
     */
    public static boolean validateMasterKey(String masterKey) {
        try {
            byte[] decoded = Base64.getDecoder().decode(masterKey);
            return decoded.length == KEY_SIZE_BYTES;
        } catch (Exception e) {
            return false;
        }
    }
}
