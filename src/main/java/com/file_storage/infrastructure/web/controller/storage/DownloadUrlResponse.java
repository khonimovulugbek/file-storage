package com.file_storage.infrastructure.web.controller.storage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadUrlResponse {
    private String fileId;
    private String downloadUrl;
    private int expiresIn;
}
