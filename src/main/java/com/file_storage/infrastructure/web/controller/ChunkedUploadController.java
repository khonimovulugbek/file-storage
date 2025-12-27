package com.file_storage.infrastructure.web.controller;

import com.file_storage.application.port.in.ChunkedUploadUseCase;
import com.file_storage.domain.model.FileChunk;
import com.file_storage.domain.model.UploadSession;
import com.file_storage.infrastructure.web.dto.request.InitiateUploadRequest;
import com.file_storage.infrastructure.web.dto.response.ApiResponse;
import com.file_storage.infrastructure.websocket.SyncWebSocketHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class ChunkedUploadController {

    private final ChunkedUploadUseCase chunkedUploadUseCase;
    private final SyncWebSocketHandler syncWebSocketHandler;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<UploadSession>> initiateUpload(
            @Valid @RequestBody InitiateUploadRequest request,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);

        UploadSession session = chunkedUploadUseCase.initiateUpload(
                request.getFileName(),
                request.getTotalSize(),
                request.getTotalChunks(),
                request.getContentType(),
                userId,
                request.getFolderId()
        );

        return ResponseEntity.ok(ApiResponse.success("Upload session initiated", session));
    }

    @PostMapping("/{sessionId}/chunk/{chunkNumber}")
    public ResponseEntity<ApiResponse<FileChunk>> uploadChunk(
            @PathVariable UUID sessionId,
            @PathVariable Integer chunkNumber,
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam("checksum") String checksum,
            Authentication authentication) throws Exception {

        UUID userId = getUserIdFromAuth(authentication);

        FileChunk uploadedChunk = chunkedUploadUseCase.uploadChunk(
                sessionId,
                chunkNumber,
                chunk.getInputStream(),
                chunk.getSize(),
                checksum,
                userId
        );

        UploadSession session = chunkedUploadUseCase.getUploadSession(sessionId, userId);
        syncWebSocketHandler.notifyUploadProgress(
                userId.toString(),
                sessionId.toString(),
                session.getProgress()
        );

        return ResponseEntity.ok(ApiResponse.success("Chunk uploaded", uploadedChunk));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<UploadSession>> getUploadSession(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        UploadSession session = chunkedUploadUseCase.getUploadSession(sessionId, userId);

        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @GetMapping("/{sessionId}/chunks")
    public ResponseEntity<ApiResponse<List<FileChunk>>> getUploadedChunks(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        List<FileChunk> chunks = chunkedUploadUseCase.getUploadedChunks(sessionId, userId);

        return ResponseEntity.ok(ApiResponse.success(chunks));
    }

    @GetMapping("/{sessionId}/missing-chunks")
    public ResponseEntity<ApiResponse<List<Integer>>> getMissingChunks(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        List<Integer> missingChunks = chunkedUploadUseCase.getMissingChunks(sessionId, userId);

        return ResponseEntity.ok(ApiResponse.success(missingChunks));
    }

    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeUpload(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        chunkedUploadUseCase.completeUpload(sessionId, userId);

        syncWebSocketHandler.broadcastFileChange(
                sessionId.toString(),
                "FILE_UPLOADED",
                userId.toString()
        );

        return ResponseEntity.ok(ApiResponse.success("Upload completed", null));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> cancelUpload(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        chunkedUploadUseCase.cancelUpload(sessionId, userId);

        return ResponseEntity.ok(ApiResponse.success("Upload cancelled", null));
    }

    private UUID getUserIdFromAuth(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
