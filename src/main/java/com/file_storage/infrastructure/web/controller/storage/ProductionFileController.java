package com.file_storage.infrastructure.web.controller.storage;

import com.file_storage.application.port.in.storage.*;
import com.file_storage.domain.model.storage.FileId;
import com.file_storage.infrastructure.web.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST Controller for production file upload/download
 * Implements hexagonal architecture - adapts HTTP to use cases
 */
@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
@Slf4j
public class ProductionFileController {
    
    private final UploadFileUseCase uploadFileUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    
    /**
     * Upload file endpoint
     * POST /api/v1/storage/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "storageType", required = false) String storageType,
            Authentication authentication) {
        
        try {
            UUID userId = authentication == null ? UUID.randomUUID() : getUserIdFromAuth(authentication);
            
            // Create upload command
            FileUploadCommand command = FileUploadCommand.builder()
                .fileContent(file.getInputStream())
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .ownerId(userId)
                .preferredStorageType(storageType)
                .build();
            
            // Execute upload use case
            FileUploadResult result = uploadFileUseCase.upload(command);
            
            // Map to response DTO
            FileUploadResponse response = FileUploadResponse.builder()
                .fileId(result.fileId().toString())
                .fileName(result.fileName())
                .fileSize(result.fileSize())
                .contentType(result.contentType())
                .checksum(result.checksum())
                .storageNodeId(result.storageNodeId())
                .uploadedAt(result.uploadedAt())
                .deduplicated(result.deduplicated())
                .build();
            
            String message = result.deduplicated()
                ? "File already exists (deduplicated)" 
                : "File uploaded successfully";
            
            return ResponseEntity.ok(ApiResponse.success(message, response));
            
        } catch (Exception e) {
            log.error("File upload failed", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }
    
    /**
     * Download file endpoint
     * GET /api/v1/storage/download/{fileId}
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(
            @PathVariable String fileId,
            Authentication authentication) {
        
        try {
            UUID userId = getUserIdFromAuth(authentication);
            
            // Create download query
            FileDownloadQuery query = FileDownloadQuery.builder()
                .fileId(FileId.fromString(fileId))
                .userId(userId)
                .build();
            
            // Execute download use case
            FileDownloadResult result = downloadFileUseCase.download(query);
            
            // Stream file to client
            InputStreamResource resource = new InputStreamResource(result.fileStream());
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + result.fileName() + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .contentLength(result.fileSize())
                .header("X-File-Checksum", result.checksum())
                .body(resource);
                
        } catch (Exception e) {
            log.error("File download failed: {}", fileId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Download failed: " + e.getMessage()));
        }
    }
    
    /**
     * Generate presigned download URL
     * GET /api/v1/storage/download-url/{fileId}
     */
    @GetMapping("/download-url/{fileId}")
    public ResponseEntity<ApiResponse<DownloadUrlResponse>> getDownloadUrl(
            @PathVariable String fileId,
            @RequestParam(value = "expiresIn", defaultValue = "300") int expiresIn,
            Authentication authentication) {
        
        try {
            UUID userId = getUserIdFromAuth(authentication);
            
            FileDownloadQuery query = FileDownloadQuery.builder()
                .fileId(FileId.fromString(fileId))
                .userId(userId)
                .build();
            
            String url = downloadFileUseCase.generateDownloadUrl(query, expiresIn);
            
            DownloadUrlResponse response = DownloadUrlResponse.builder()
                .fileId(fileId)
                .downloadUrl(url)
                .expiresIn(expiresIn)
                .build();
            
            return ResponseEntity.ok(ApiResponse.success("Download URL generated", response));
            
        } catch (Exception e) {
            log.error("Failed to generate download URL: {}", fileId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("URL generation failed: " + e.getMessage()));
        }
    }
    
    private UUID getUserIdFromAuth(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
