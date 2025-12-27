package com.file_storage.infrastructure.mapper;

import com.file_storage.domain.model.User;
import com.file_storage.infrastructure.persistence.entity.user.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public User toDomain(UserEntity entity) {
        if (entity == null) return null;
        
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .status(User.UserStatus.valueOf(entity.getStatus().name()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public UserEntity toEntity(User domain) {
        if (domain == null) return null;
        
        return UserEntity.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .email(domain.getEmail())
                .passwordHash(domain.getPasswordHash())
                .status(UserEntity.UserStatus.valueOf(domain.getStatus().name()))
                .build();
    }
}
