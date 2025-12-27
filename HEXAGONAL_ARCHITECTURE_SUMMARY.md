# Hexagonal Architecture Refactoring Summary

## Overview
This project has been refactored to follow **Hexagonal Architecture** (Ports and Adapters) principles.

## Architecture Layers

### 1. Domain Layer (`domain/`)
- **Pure business logic** - no dependencies on infrastructure or frameworks
- Contains domain models: `File`, `Folder`, `User`, `FileUploadRequest`
- Framework-agnostic and technology-independent

### 2. Application Layer (`application/`)
- **Business use cases and orchestration**
- Depends ONLY on domain models and port interfaces
- No direct dependencies on infrastructure

#### Input Ports (`application/port/in/`)
- `FileUseCase` - File operations interface
- `FolderUseCase` - Folder operations interface  
- `UserUseCase` - User operations interface

#### Output Ports (`application/port/out/`)
- `FilePort` - File persistence operations
- `FileStoragePort` - File storage operations (MinIO)
- `FolderPort` - Folder persistence operations
- `UserPort` - User persistence operations
- `CachePort` - Caching operations
- `PasswordEncoderPort` - Password encoding operations

#### Services (`application/service/`)
- `FileService` - Implements FileUseCase, depends on ports
- `FolderService` - Implements FolderUseCase, depends on ports
- `UserService` - Implements UserUseCase, depends on ports

### 3. Infrastructure Layer (`infrastructure/`)
- **Technical implementations** of ports
- Adapters that connect external systems to the application core

#### Persistence Adapters (`infrastructure/persistence/adapter/`)
- `FilePortAdapter` - Implements FilePort using JPA
- `FolderPortAdapter` - Implements FolderPort using JPA
- `UserPortAdapter` - Implements UserPort using JPA

#### Storage Adapter (`infrastructure/storage/`)
- `MinioStorageAdapter` - Implements FileStoragePort using MinIO

#### Cache Adapter (`infrastructure/cache/`)
- `CacheService` - Implements CachePort using Redis

#### Security Adapter (`infrastructure/security/`)
- `PasswordEncoderAdapter` - Implements PasswordEncoderPort using Spring Security

#### Web Controllers (`infrastructure/web/controller/`)
- `FileController` - REST API endpoints, adapts MultipartFile to FileUploadRequest
- `FolderController` - REST API endpoints
- `AuthController` - Authentication endpoints

## Key Improvements

### ✅ Fixed Violations

1. **Application layer no longer depends on infrastructure**
   - Removed direct dependencies on repositories, entities, and mappers
   - Services now depend only on port interfaces

2. **Framework-agnostic domain**
   - Removed Spring's `MultipartFile` from `FileUseCase`
   - Created domain model `FileUploadRequest` instead

3. **Proper dependency direction**
   - Infrastructure adapters implement application ports
   - Dependencies point inward: Infrastructure → Application → Domain

4. **Separation of concerns**
   - Controllers handle framework-specific types (MultipartFile)
   - Application layer works with pure domain models
   - Infrastructure adapters bridge the gap

## Dependency Flow

```
Infrastructure Layer (Adapters)
        ↓ implements
Application Layer (Ports & Services)
        ↓ uses
Domain Layer (Models)
```

## Benefits

1. **Testability** - Application logic can be tested without infrastructure
2. **Flexibility** - Easy to swap implementations (e.g., MinIO → S3)
3. **Maintainability** - Clear separation of concerns
4. **Technology Independence** - Core business logic is framework-agnostic
5. **SOLID Principles** - Dependency Inversion Principle properly applied
