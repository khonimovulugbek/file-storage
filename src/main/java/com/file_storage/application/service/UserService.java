package com.file_storage.application.service;

import com.file_storage.application.port.in.UserUseCase;
import com.file_storage.application.port.out.PasswordEncoderPort;
import com.file_storage.application.port.out.UserPort;
import com.file_storage.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserUseCase {

    private final UserPort userPort;
    private final PasswordEncoderPort passwordEncoderPort;

    @Override
    @Transactional
    public User register(String username, String email, String password) {
        if (userPort.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        if (userPort.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(email)
                .passwordHash(passwordEncoderPort.encode(password))
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User saved = userPort.save(user);
        log.info("User registered successfully: {}", saved.getUsername());

        return saved;
    }

    @Override
    public User authenticate(String username, String password) {
        User user = userPort.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoderPort.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

    @Override
    public User getUserById(UUID userId) {
        return userPort.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserByUsername(String username) {
        return userPort.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
