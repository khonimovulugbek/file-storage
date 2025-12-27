package com.file_storage.infrastructure.persistence.adapter;

import com.file_storage.application.port.out.UserPort;
import com.file_storage.domain.model.User;
import com.file_storage.infrastructure.mapper.UserMapper;
import com.file_storage.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPortAdapter implements UserPort {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        return userMapper.toDomain(userRepository.save(userMapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
