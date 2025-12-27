package com.file_storage.infrastructure.persistence.repository.storage;

import com.file_storage.infrastructure.persistence.entity.storage.EncryptionKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EncryptionKeyJpaRepository extends JpaRepository<EncryptionKeyEntity, String> {
}
