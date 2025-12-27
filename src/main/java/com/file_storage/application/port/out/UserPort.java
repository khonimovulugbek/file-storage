package com.file_storage.application.port.out;

import com.file_storage.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserPort {
    User save(User user);
    Optional<User> findById(UUID userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
