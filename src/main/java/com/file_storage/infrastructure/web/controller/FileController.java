package com.file_storage.infrastructure.web.controller;

import com.file_storage.application.port.in.FileUseCase;
import com.file_storage.domain.model.File;
import com.file_storage.domain.model.FileUploadRequest;
import com.file_storage.infrastructure.web.dto.response.ApiResponse;
import com.file_storage.infrastructure.web.dto.response.FileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    private final FileUseCase fileUseCase;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) UUID folderId,
            Authentication authentication) throws Exception {
        
        UUID userId = getUserIdFromAuth(authentication);
        
        FileUploadRequest request = FileUploadRequest.builder()
                .inputStream(file.getInputStream())
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();
        
        File uploadedFile = fileUseCase.uploadFile(request, userId, folderId);
        FileResponse response = mapToFileResponse(uploadedFile);
        
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<FileResponse>> getFileMetadata(
            @PathVariable UUID fileId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuth(authentication);
        File file = fileUseCase.getFileMetadata(fileId, userId);
        FileResponse response = mapToFileResponse(file);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable UUID fileId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuth(authentication);
        File file = fileUseCase.getFileMetadata(fileId, userId);
        InputStream inputStream = fileUseCase.downloadFile(fileId, userId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(new InputStreamResource(inputStream));
    }

    @GetMapping("/{fileId}/download-url")
    public ResponseEntity<ApiResponse<String>> getDownloadUrl(
            @PathVariable UUID fileId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuth(authentication);
        String url = fileUseCase.getDownloadUrl(fileId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("Download URL generated", url));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FileResponse>>> listFiles(Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        List<File> files = fileUseCase.listUserFiles(userId);
        List<FileResponse> response = files.stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<ApiResponse<List<FileResponse>>> listFolderFiles(
            @PathVariable UUID folderId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuth(authentication);
        List<File> files = fileUseCase.listFolderFiles(folderId, userId);
        List<FileResponse> response = files.stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FileResponse>>> searchFiles(
            @RequestParam String query,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuth(authentication);
        List<File> files = fileUseCase.searchFiles(query, userId);
        List<FileResponse> response = files.stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable UUID fileId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuth(authentication);
        fileUseCase.deleteFile(fileId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }

    private UUID getUserIdFromAuth(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private FileResponse mapToFileResponse(File file) {
        return FileResponse.builder()
                .id(file.getId())
                .name(file.getName())
                .size(file.getSize())
                .contentType(file.getContentType())
                .status(file.getStatus().name())
                .ownerId(file.getOwnerId())
                .parentFolderId(file.getParentFolderId())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }
}
