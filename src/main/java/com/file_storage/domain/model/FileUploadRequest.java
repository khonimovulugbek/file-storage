package com.file_storage.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;

@Getter
@Builder
public class FileUploadRequest {
    private final InputStream inputStream;
    private final String fileName;
    private final String contentType;
    private final long size;
}
