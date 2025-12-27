package com.file_storage.infrastructure.persistence.entity.file;

import com.file_storage.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetaDataEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(nullable = false)
    private Long size;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(nullable = false, length = 64)
    private String checksum;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FileStatus status = FileStatus.ACTIVE;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "parent_folder_id")
    private UUID parentFolderId;

    @Column(name = "storage_location", nullable = false, length = 1000)
    private String storageLocation;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum FileStatus {
        PENDING,
        ACTIVE,
        DELETED
    }
}
