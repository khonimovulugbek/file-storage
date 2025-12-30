package com.file_storage.application.port.in.storage;

import com.file_storage.domain.model.storage.FileId;
import lombok.Builder;

import java.io.InputStream;

public interface DownloadFileUseCase {
    FileDownloadResult download(FileDownloadQuery query);

    @Builder
    record FileDownloadQuery(FileId fileId) {
    }

    @Builder
    record FileDownloadResult(InputStream fileStream,
                              String fileName,
                              String contentType,
                              long fileSize,
                              String checksum
    ) {
    }


}
