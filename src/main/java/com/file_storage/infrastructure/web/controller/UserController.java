package com.file_storage.infrastructure.web.controller;

import com.file_storage.application.port.in.UserUseCase;
import com.file_storage.domain.model.User;
import com.file_storage.infrastructure.web.dto.response.ApiResponse;
import com.file_storage.infrastructure.web.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserUseCase userUseCase;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userUseCase.getUserByUsername(username);
        
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID userId) {
        User user = userUseCase.getUserById(userId);
        
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

