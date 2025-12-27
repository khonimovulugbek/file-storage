package com.file_storage.application.port.in;

import com.file_storage.domain.model.User;

import java.util.UUID;

public interface UserUseCase {
    User register(String username, String email, String password);
    User authenticate(String username, String password);
    User getUserById(UUID userId);
    User getUserByUsername(String username);
}
