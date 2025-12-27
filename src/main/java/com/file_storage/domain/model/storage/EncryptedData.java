package com.file_storage.domain.model.storage;

import java.util.Base64;

/**
 * Value Object representing encrypted data
 * Format: algorithm:iv:ciphertext (Base64 encoded)
 *
 * @param iv Initialization vector
 */
public record EncryptedData(String algorithm, byte[] iv, byte[] ciphertext) {
    public EncryptedData {
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("Algorithm cannot be null or empty");
        }
        if (iv == null || iv.length == 0) {
            throw new IllegalArgumentException("IV cannot be null or empty");
        }
        if (ciphertext == null || ciphertext.length == 0) {
            throw new IllegalArgumentException("Ciphertext cannot be null or empty");
        }

    }

    /**
     * Serialize to storable format: algorithm:base64(iv):base64(ciphertext)
     */
    public String serialize() {
        String ivBase64 = Base64.getEncoder().encodeToString(iv);
        String ciphertextBase64 = Base64.getEncoder().encodeToString(ciphertext);
        return algorithm + ":" + ivBase64 + ":" + ciphertextBase64;
    }

    /**
     * Deserialize from stored format
     */
    public static EncryptedData deserialize(String serialized) {
        String[] parts = serialized.split(":", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid encrypted data format");
        }

        String algorithm = parts[0];
        byte[] iv = Base64.getDecoder().decode(parts[1]);
        byte[] ciphertext = Base64.getDecoder().decode(parts[2]);

        return new EncryptedData(algorithm, iv, ciphertext);
    }
}
