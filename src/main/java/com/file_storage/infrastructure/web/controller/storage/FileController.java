package com.file_storage.infrastructure.web.controller.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.file_storage.application.port.in.storage.DownloadFileUseCase;
import com.file_storage.application.port.in.storage.UploadFileUseCase;
import com.file_storage.domain.model.storage.FileId;
import com.file_storage.infrastructure.web.dto.response.ApiResponse;
import com.file_storage.infrastructure.web.dto.response.storage.FileUploadResponse;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
@Slf4j
public class FileController {
    private final UploadFileUseCase uploadFileUseCase;
    private final DownloadFileUseCase downloadFileUseCase;

    public FileController(UploadFileUseCase uploadFileUseCase, DownloadFileUseCase downloadFileUseCase) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.downloadFileUseCase = downloadFileUseCase;
    }

    @PostMapping
    public ApiResponse<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            UploadFileUseCase.FileUploadCommand command = UploadFileUseCase.FileUploadCommand.builder()
                    .content(file.getInputStream())
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .ownerId(null)
                    .build();
            UploadFileUseCase.FileUploadResult result = uploadFileUseCase.upload(command);
            FileUploadResponse response = FileUploadResponse.builder()
                    .fileName(result.fileName())
                    .fileSize(result.fileSize())
                    .fileId(result.fileId().value())
                    .contentType(result.contentType())
                    .checksum(result.checksum())
                    .storageNodeId(result.storageNodeId())
                    .absolutePath(result.absolutePath())
                    .createdAt(result.createdAt())
                    .updatedAt(result.updatedAt())
                    .build();
            return ApiResponse.success(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileId) {
        DownloadFileUseCase.FileDownloadQuery query = DownloadFileUseCase.FileDownloadQuery.builder().fileId(new FileId(fileId)).build();
        DownloadFileUseCase.FileDownloadResult result = downloadFileUseCase.download(query);
        InputStreamResource resource = new InputStreamResource(result.fileStream());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.fileName())
                .contentType(MediaType.parseMediaType(result.contentType()))
                .contentLength(result.fileSize())
                .header("X-File-Checksum", result.checksum())
                .body(resource);
    }
}
