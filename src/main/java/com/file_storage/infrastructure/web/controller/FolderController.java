package com.file_storage.infrastructure.web.controller;

import com.file_storage.application.port.in.FolderUseCase;
import com.file_storage.domain.model.Folder;
import com.file_storage.infrastructure.web.dto.request.CreateFolderRequest;
import com.file_storage.infrastructure.web.dto.response.ApiResponse;
import com.file_storage.infrastructure.web.dto.response.FolderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
public class FolderController {
    private final FolderUseCase folderUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            @Valid @RequestBody CreateFolderRequest request,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuth(authentication);
        Folder folder = folderUseCase.createFolder(request.getName(), request.getParentFolderId(), userId);
        FolderResponse response = mapToFolderResponse(folder);
        
        return ResponseEntity.ok(ApiResponse.success("Folder created successfully", response));
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<ApiResponse<FolderResponse>> getFolder(
            @PathVariable UUID folderId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuth(authentication);
        Folder folder = folderUseCase.getFolderById(folderId, userId);
        FolderResponse response = mapToFolderResponse(folder);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FolderResponse>>> listFolders(Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        List<Folder> folders = folderUseCase.listUserFolders(userId);
        List<FolderResponse> response = folders.stream()
                .map(this::mapToFolderResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{folderId}/subfolders")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> listSubFolders(
            @PathVariable UUID folderId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuth(authentication);
        List<Folder> folders = folderUseCase.listSubFolders(folderId, userId);
        List<FolderResponse> response = folders.stream()
                .map(this::mapToFolderResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<ApiResponse<FolderResponse>> updateFolder(
            @PathVariable UUID folderId,
            @RequestParam String name,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuth(authentication);
        Folder folder = folderUseCase.updateFolder(folderId, name, userId);
        FolderResponse response = mapToFolderResponse(folder);
        
        return ResponseEntity.ok(ApiResponse.success("Folder updated successfully", response));
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            @PathVariable UUID folderId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuth(authentication);
        folderUseCase.deleteFolder(folderId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("Folder deleted successfully", null));
    }

    private UUID getUserIdFromAuth(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private FolderResponse mapToFolderResponse(Folder folder) {
        return FolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .parentFolderId(folder.getParentFolderId())
                .ownerId(folder.getOwnerId())
                .path(folder.getPath())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }
}
