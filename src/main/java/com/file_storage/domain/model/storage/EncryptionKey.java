package com.file_storage.domain.model.storage;

import lombok.Getter;

/**
 * Value Object representing encryption key reference
 * Actual key stored in external vault (AWS KMS, HashiCorp Vault)
 *
 * @param keyRef     Reference to key in vault
 * @param keyVaultId Vault identifier
 */
public record EncryptionKey(String keyRef, String keyVaultId, EncryptionAlgorithm algorithm) {
    public EncryptionKey {
        if (keyRef == null || keyRef.isBlank()) {
            throw new IllegalArgumentException("Key reference cannot be null or empty");
        }
        if (keyVaultId == null || keyVaultId.isBlank()) {
            throw new IllegalArgumentException("Key vault ID cannot be null or empty");
        }
        if (algorithm == null) {
            throw new IllegalArgumentException("Encryption algorithm cannot be null");
        }

    }

    @Getter
    public enum EncryptionAlgorithm {
        AES_256_GCM("AES-256-GCM"),
        AES_256_CBC("AES-256-CBC");

        private final String displayName;

        EncryptionAlgorithm(String displayName) {
            this.displayName = displayName;
        }

    }
}
