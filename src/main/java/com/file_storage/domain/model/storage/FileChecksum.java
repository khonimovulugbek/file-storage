package com.file_storage.domain.model.storage;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Value Object representing file checksum for integrity verification
 */
@Getter
@EqualsAndHashCode
public class FileChecksum {
    private final ChecksumAlgorithm algorithm;
    private final String hash;

    public FileChecksum(ChecksumAlgorithm algorithm, String hash) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Checksum algorithm cannot be null");
        }
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Checksum hash cannot be null or empty");
        }
        if (!isValidHash(algorithm, hash)) {
            throw new IllegalArgumentException("Invalid hash format for algorithm: " + algorithm);
        }

        this.algorithm = algorithm;
        this.hash = hash.toLowerCase();
    }

    private boolean isValidHash(ChecksumAlgorithm algorithm, String hash) {
        return switch (algorithm) {
            case SHA256 -> hash.matches("^[a-fA-F0-9]{64}$");
            case MD5 -> hash.matches("^[a-fA-F0-9]{32}$");
        };
    }

    public enum ChecksumAlgorithm {
        SHA256("SHA-256"),
        MD5("MD5");

        private final String displayName;

        ChecksumAlgorithm(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Override
    public String toString() {
        return algorithm + ":" + hash;
    }
}
