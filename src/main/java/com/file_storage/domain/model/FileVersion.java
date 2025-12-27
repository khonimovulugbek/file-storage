package com.file_storage.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileVersion {
    private UUID id;
    private UUID fileId;
    private Integer versionNumber;
    private Long size;
    private String checksum;
    private String storageLocation;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private String comment;
    private boolean isCurrent;

    public void markAsCurrent() {
        this.isCurrent = true;
    }

    public void markAsOld() {
        this.isCurrent = false;
    }
}
