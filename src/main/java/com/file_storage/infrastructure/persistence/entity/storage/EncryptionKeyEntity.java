package com.file_storage.infrastructure.persistence.entity.storage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "encryption_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncryptionKeyEntity {
    
    @Id
    @Column(name = "key_ref", length = 100)
    private String keyRef;
    
    @Column(name = "encrypted_key", columnDefinition = "TEXT", nullable = false)
    private String encryptedKey;
    
    @Column(name = "algorithm", length = 50, nullable = false)
    private String algorithm;
    
    @Column(name = "vault_id", length = 255, nullable = false)
    private String vaultId;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "status", length = 20, nullable = false)
    private String status;
}
