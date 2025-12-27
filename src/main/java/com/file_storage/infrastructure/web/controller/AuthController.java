package com.file_storage.infrastructure.web.controller;

import com.file_storage.application.port.in.UserUseCase;
import com.file_storage.domain.model.User;
import com.file_storage.infrastructure.security.JwtService;
import com.file_storage.infrastructure.web.dto.request.LoginRequest;
import com.file_storage.infrastructure.web.dto.request.RegisterRequest;
import com.file_storage.infrastructure.web.dto.response.ApiResponse;
import com.file_storage.infrastructure.web.dto.response.AuthResponse;
import com.file_storage.infrastructure.web.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserUseCase userUseCase;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userUseCase.register(request.getUsername(), request.getEmail(), request.getPassword());
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();

        return ResponseEntity.ok(ApiResponse.success("User registered successfully", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        User user = userUseCase.authenticate(request.getUsername(), request.getPassword());
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }
}
