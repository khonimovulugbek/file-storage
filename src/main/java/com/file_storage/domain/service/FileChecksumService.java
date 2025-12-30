package com.file_storage.domain.service;

import com.file_storage.domain.model.storage.FileChecksum;

import java.io.InputStream;
import java.security.MessageDigest;

public class FileChecksumService {

    public FileChecksum calculateChecksum(InputStream content, FileChecksum.ChecksumAlgorithm algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.displayName());
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = content.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return new FileChecksum(algorithm, hexString.toString());

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }
}
