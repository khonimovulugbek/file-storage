package com.file_storage.infrastructure.persistence.entity.folder;

import com.file_storage.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "folders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "parent_folder_id")
    private UUID parentFolderId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 2000)
    private String path;
}
