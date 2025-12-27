# File Storage System - High Level Design (HLD)

## Architecture Overview

This document describes the High Level Design of a distributed file storage system built using **Hexagonal Architecture (Ports & Adapters)** with **Domain-Driven Design (DDD)** principles.

### Architecture Pattern: Hexagonal Architecture + DDD

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
│  (Web UI, Mobile Apps, CLI, Third-party Integrations)           │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│                    Infrastructure Layer                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              REST API Controllers (Adapters)              │  │
│  │  - FileController                                         │  │
│  │  - UserController                                         │  │
│  │  - FolderController                                       │  │
│  │  - SearchController                                       │  │
│  │  - ShareController                                        │  │
│  └────────────────────┬─────────────────────────────────────┘  │
└────────────────────────┼────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│                    Application Layer                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                  Use Cases (Ports)                        │  │
│  │  - FileUseCase                                            │  │
│  │  - UserUseCase                                            │  │
│  │  - FolderUseCase                                          │  │
│  │  - SearchUseCase                                          │  │
│  │  - ShareUseCase                                           │  │
│  └────────────────────┬─────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Application Services                         │  │
│  │  - FileService                                            │  │
│  │  - UserService                                            │  │
│  │  - FolderService                                          │  │
│  │  - SearchService                                          │  │
│  │  - ShareService                                           │  │
│  └────────────────────┬─────────────────────────────────────┘  │
└────────────────────────┼────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│                      Domain Layer (Core)                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                  Domain Models                            │  │
│  │  - File (Aggregate Root)                                  │  │
│  │  - User (Aggregate Root)                                  │  │
│  │  - Folder (Aggregate Root)                                │  │
│  │  - Permission (Value Object)                              │  │
│  │  - FileVersion (Entity)                                   │  │
│  │  - Share (Entity)                                         │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Domain Services                              │  │
│  │  - FileStorageService                                     │  │
│  │  - PermissionService                                      │  │
│  │  - VersioningService                                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Repository Interfaces (Ports)                │  │
│  │  - FileRepository                                         │  │
│  │  - UserRepository                                         │  │
│  │  - FolderRepository                                       │  │
│  │  - PermissionRepository                                   │  │
│  │  - VersionRepository                                      │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│              Infrastructure Layer (Adapters)                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         Persistence Adapters (JPA/Hibernate)              │  │
│  │  - FileRepositoryAdapter                                  │  │
│  │  - UserRepositoryAdapter                                  │  │
│  │  - FolderRepositoryAdapter                                │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Storage Adapters                             │  │
│  │  - LocalFileSystemAdapter                                 │  │
│  │  - S3StorageAdapter                                       │  │
│  │  - MinIOStorageAdapter                                    │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Cache Adapters                               │  │
│  │  - RedisAdapter                                           │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Message Queue Adapters                       │  │
│  │  - RabbitMQAdapter / KafkaAdapter                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1. System Components

### 1.1 Client Interface Layer

The client interface provides multiple ways to interact with the file storage system:

#### **Web Application**
- Modern React/Vue.js SPA
- File upload/download with drag-and-drop
- Folder navigation and organization
- Real-time collaboration features
- Search and filtering capabilities

#### **Mobile Applications**
- iOS and Android native apps
- Offline file access
- Camera integration for photo uploads
- Push notifications for sharing events

#### **API Clients**
- RESTful API for third-party integrations
- SDK libraries (Java, Python, JavaScript)
- CLI tool for automation

---

### 1.2 API Gateway / Load Balancer

#### **Responsibilities:**
- Request routing and load distribution
- Rate limiting and throttling
- API versioning support
- SSL/TLS termination
- Request/response transformation
- Authentication token validation

#### **Technology Stack:**
- **NGINX** or **Kong API Gateway**
- **AWS ALB/ELB** or **HAProxy**

---

### 1.3 Application Services (Microservices Architecture)

The system is decomposed into multiple microservices following DDD bounded contexts:

#### **1.3.1 Authentication & Authorization Service**

**Bounded Context:** Identity & Access Management

**Responsibilities:**
- User authentication (OAuth2, JWT)
- Multi-factor authentication (MFA)
- Role-based access control (RBAC)
- Permission management
- Session management
- API key management

**Domain Model:**
```java
// Aggregate Root
public class User {
    private UserId id;
    private Username username;
    private Email email;
    private Password password; // Value Object (hashed)
    private Set<Role> roles;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}

// Value Objects
public record UserId(UUID value) {}
public record Email(String value) {}
public record Username(String value) {}
```

**API Endpoints:**
```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh-token
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
GET    /api/v1/auth/verify-email/{token}
POST   /api/v1/auth/mfa/enable
POST   /api/v1/auth/mfa/verify
```

---

#### **1.3.2 File Metadata Service**

**Bounded Context:** File Management

**Responsibilities:**
- File metadata CRUD operations
- File versioning
- File lifecycle management
- Metadata indexing for search
- File organization (folders, tags)

**Domain Model:**
```java
// Aggregate Root
public class File {
    private FileId id;
    private FileName name;
    private FileSize size;
    private ContentType contentType;
    private Checksum checksum; // SHA-256 hash
    private FileStatus status; // PENDING, ACTIVE, DELETED
    private UserId ownerId;
    private FolderId parentFolderId;
    private StorageLocation storageLocation;
    private List<FileVersion> versions;
    private Set<Tag> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Domain methods
    public void markAsDeleted() { /* soft delete */ }
    public void addVersion(FileVersion version) { /* versioning logic */ }
    public boolean isOwnedBy(UserId userId) { /* ownership check */ }
}

// Entity
public class FileVersion {
    private VersionId id;
    private FileId fileId;
    private int versionNumber;
    private long size;
    private String storageLocation;
    private LocalDateTime createdAt;
}

// Value Objects
public record FileId(UUID value) {}
public record FileName(String value) {}
public record FileSize(long bytes) {}
public record Checksum(String hash) {}
public record StorageLocation(String path) {}
```

**API Endpoints:**
```
POST   /api/v1/files                    # Upload file
GET    /api/v1/files/{fileId}           # Download file
GET    /api/v1/files/{fileId}/metadata  # Get metadata
PUT    /api/v1/files/{fileId}/metadata  # Update metadata
DELETE /api/v1/files/{fileId}           # Delete file
GET    /api/v1/files                    # List files (paginated)
GET    /api/v1/files/{fileId}/versions  # Get file versions
POST   /api/v1/files/{fileId}/restore   # Restore version
GET    /api/v1/files/{fileId}/download  # Download specific version
POST   /api/v1/files/bulk-upload        # Bulk upload
DELETE /api/v1/files/bulk-delete        # Bulk delete
```

---

#### **1.3.3 Folder Management Service**

**Bounded Context:** File Organization

**Responsibilities:**
- Folder hierarchy management
- Folder permissions
- Folder sharing
- Nested folder operations

**Domain Model:**
```java
// Aggregate Root
public class Folder {
    private FolderId id;
    private FolderName name;
    private FolderId parentFolderId;
    private UserId ownerId;
    private List<FolderId> childFolders;
    private List<FileId> files;
    private FolderPath path; // Full path for quick lookup
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Domain methods
    public void addFile(FileId fileId) { /* add file */ }
    public void removeFile(FileId fileId) { /* remove file */ }
    public void move(FolderId newParentId) { /* move folder */ }
}

// Value Objects
public record FolderId(UUID value) {}
public record FolderName(String value) {}
public record FolderPath(String value) {}
```

**API Endpoints:**
```
POST   /api/v1/folders                  # Create folder
GET    /api/v1/folders/{folderId}       # Get folder details
PUT    /api/v1/folders/{folderId}       # Update folder
DELETE /api/v1/folders/{folderId}       # Delete folder
GET    /api/v1/folders/{folderId}/files # List files in folder
POST   /api/v1/folders/{folderId}/move  # Move folder
GET    /api/v1/folders/tree             # Get folder tree
```

---

#### **1.3.4 Storage Coordination Service**

**Bounded Context:** Physical Storage Management

**Responsibilities:**
- Coordinate file upload/download with storage nodes
- Storage node selection (based on load, location, capacity)
- Replication management
- Data integrity verification
- Storage node health monitoring

**Domain Model:**
```java
// Aggregate Root
public class StorageNode {
    private StorageNodeId id;
    private NodeAddress address;
    private StorageCapacity capacity;
    private NodeStatus status; // ACTIVE, MAINTENANCE, OFFLINE
    private DataCenter dataCenter;
    private LocalDateTime lastHealthCheck;
    
    // Domain methods
    public boolean hasCapacity(FileSize size) { /* check capacity */ }
    public void markAsOffline() { /* change status */ }
}

// Value Objects
public record StorageNodeId(UUID value) {}
public record NodeAddress(String host, int port) {}
public record StorageCapacity(long totalBytes, long usedBytes) {
    public long availableBytes() { return totalBytes - usedBytes; }
}
```

**API Endpoints:**
```
POST   /api/v1/storage/upload-url       # Get presigned upload URL
POST   /api/v1/storage/download-url     # Get presigned download URL
GET    /api/v1/storage/nodes            # List storage nodes
GET    /api/v1/storage/nodes/{nodeId}   # Get node status
POST   /api/v1/storage/replicate        # Trigger replication
GET    /api/v1/storage/health           # Health check
```

---

#### **1.3.5 Search Service**

**Bounded Context:** File Discovery

**Responsibilities:**
- Full-text search on file names and metadata
- Tag-based search
- Advanced filtering (size, type, date)
- Search result ranking
- Search analytics

**Technology Stack:**
- **Elasticsearch** or **Apache Solr**
- **Redis** for search result caching

**API Endpoints:**
```
GET    /api/v1/search?q={query}                    # Search files
GET    /api/v1/search/suggestions?q={query}        # Auto-complete
GET    /api/v1/search/advanced                     # Advanced search
POST   /api/v1/search/filters                      # Apply filters
```

---

#### **1.3.6 Sharing & Collaboration Service**

**Bounded Context:** Collaboration

**Responsibilities:**
- File/folder sharing
- Permission management (read, write, delete)
- Share link generation
- Expiring shares
- Collaboration notifications

**Domain Model:**
```java
// Aggregate Root
public class Share {
    private ShareId id;
    private FileId fileId;
    private UserId ownerId;
    private Set<UserId> sharedWithUsers;
    private SharePermission permission; // READ, WRITE, DELETE
    private ShareLink shareLink; // Optional public link
    private LocalDateTime expiresAt;
    private ShareStatus status; // ACTIVE, EXPIRED, REVOKED
    private LocalDateTime createdAt;
    
    // Domain methods
    public void revokeAccess(UserId userId) { /* revoke */ }
    public boolean isExpired() { /* check expiration */ }
    public void grantPermission(UserId userId, SharePermission permission) { /* grant */ }
}

// Value Objects
public record ShareId(UUID value) {}
public record ShareLink(String token) {}
public enum SharePermission { READ, WRITE, DELETE }
```

**API Endpoints:**
```
POST   /api/v1/shares                   # Create share
GET    /api/v1/shares/{shareId}         # Get share details
DELETE /api/v1/shares/{shareId}         # Revoke share
GET    /api/v1/shares/file/{fileId}     # Get shares for file
POST   /api/v1/shares/link              # Generate public link
GET    /api/v1/shares/link/{token}      # Access via public link
PUT    /api/v1/shares/{shareId}/permissions # Update permissions
```

---

#### **1.3.7 Notification Service**

**Bounded Context:** User Communication

**Responsibilities:**
- Email notifications
- Push notifications
- In-app notifications
- Notification preferences management

**API Endpoints:**
```
GET    /api/v1/notifications            # Get user notifications
PUT    /api/v1/notifications/{id}/read  # Mark as read
DELETE /api/v1/notifications/{id}       # Delete notification
PUT    /api/v1/notifications/preferences # Update preferences
```

---

## 2. Data Storage Architecture

### 2.1 Metadata Database (PostgreSQL)

**Purpose:** Store structured metadata about files, users, permissions, and relationships.

#### **Database Schema:**

```sql
-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Files Table
CREATE TABLE files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(500) NOT NULL,
    size BIGINT NOT NULL,
    content_type VARCHAR(100),
    checksum VARCHAR(64) NOT NULL, -- SHA-256
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    owner_id UUID NOT NULL REFERENCES users(id),
    parent_folder_id UUID REFERENCES folders(id),
    storage_location VARCHAR(1000) NOT NULL,
    storage_node_id UUID REFERENCES storage_nodes(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Folders Table
CREATE TABLE folders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    parent_folder_id UUID REFERENCES folders(id),
    owner_id UUID NOT NULL REFERENCES users(id),
    path VARCHAR(2000) NOT NULL, -- Full path for quick lookup
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_folder_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- File Versions Table
CREATE TABLE file_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_id UUID NOT NULL REFERENCES files(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    size BIGINT NOT NULL,
    storage_location VARCHAR(1000) NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    UNIQUE(file_id, version_number)
);

-- Permissions Table
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_type VARCHAR(20) NOT NULL, -- FILE, FOLDER
    resource_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    permission_type VARCHAR(20) NOT NULL, -- READ, WRITE, DELETE
    granted_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    UNIQUE(resource_type, resource_id, user_id, permission_type)
);

-- Shares Table
CREATE TABLE shares (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_id UUID REFERENCES files(id) ON DELETE CASCADE,
    folder_id UUID REFERENCES folders(id) ON DELETE CASCADE,
    owner_id UUID NOT NULL REFERENCES users(id),
    share_link_token VARCHAR(255) UNIQUE,
    permission_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    CONSTRAINT chk_resource CHECK (
        (file_id IS NOT NULL AND folder_id IS NULL) OR
        (file_id IS NULL AND folder_id IS NOT NULL)
    )
);

-- Share Users (Many-to-Many)
CREATE TABLE share_users (
    share_id UUID NOT NULL REFERENCES shares(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (share_id, user_id)
);

-- Tags Table
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- File Tags (Many-to-Many)
CREATE TABLE file_tags (
    file_id UUID NOT NULL REFERENCES files(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (file_id, tag_id)
);

-- Storage Nodes Table
CREATE TABLE storage_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    total_capacity_bytes BIGINT NOT NULL,
    used_capacity_bytes BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    data_center VARCHAR(100),
    last_health_check TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Performance
CREATE INDEX idx_files_owner ON files(owner_id);
CREATE INDEX idx_files_folder ON files(parent_folder_id);
CREATE INDEX idx_files_status ON files(status);
CREATE INDEX idx_files_created_at ON files(created_at DESC);
CREATE INDEX idx_folders_owner ON folders(owner_id);
CREATE INDEX idx_folders_parent ON folders(parent_folder_id);
CREATE INDEX idx_folders_path ON folders(path);
CREATE INDEX idx_permissions_resource ON permissions(resource_type, resource_id);
CREATE INDEX idx_permissions_user ON permissions(user_id);
CREATE INDEX idx_shares_file ON shares(file_id);
CREATE INDEX idx_shares_folder ON shares(folder_id);
CREATE INDEX idx_shares_token ON shares(share_link_token);
CREATE INDEX idx_file_versions_file ON file_versions(file_id);
```

---

### 2.2 Storage Nodes

#### **Storage Strategy Options:**

##### **Option 1: Distributed File System (Recommended for Scale)**
- **Technology:** HDFS, GlusterFS, or Ceph
- **Pros:** Built-in replication, fault tolerance, horizontal scalability
- **Cons:** Complex setup and maintenance

##### **Option 2: Object Storage (Recommended for Cloud)**
- **Technology:** Amazon S3, Google Cloud Storage, MinIO (self-hosted)
- **Pros:** Highly scalable, durable, managed service
- **Cons:** Vendor lock-in (for cloud), cost at scale

##### **Option 3: Hybrid Approach**
- **Hot Storage:** SSD-based block storage for frequently accessed files
- **Cold Storage:** Object storage (S3 Glacier) for archival
- **Lifecycle Policies:** Automatic migration based on access patterns

#### **Storage Node Architecture:**

```
┌─────────────────────────────────────────────────────────────┐
│                    Storage Coordinator                       │
│  - Node Selection Algorithm                                  │
│  - Load Balancing                                            │
│  - Replication Management                                    │
└────────────┬────────────────────────────────────────────────┘
             │
     ┌───────┴───────┬───────────┬───────────┐
     │               │           │           │
┌────▼────┐    ┌────▼────┐ ┌───▼─────┐ ┌───▼─────┐
│ Node 1  │    │ Node 2  │ │ Node 3  │ │ Node N  │
│ (DC-1)  │    │ (DC-1)  │ │ (DC-2)  │ │ (DC-2)  │
└─────────┘    └─────────┘ └─────────┘ └─────────┘
```

#### **Replication Strategy:**

1. **Synchronous Replication:** Write to primary + 1 replica before acknowledging
2. **Asynchronous Replication:** Write to primary, then replicate in background
3. **Erasure Coding:** For cold storage (e.g., Reed-Solomon encoding)

**Replication Factor:** 3 (configurable)
- 1 primary copy
- 2 replica copies in different availability zones

---

### 2.3 Caching Layer

#### **Redis Cache Architecture:**

```
┌─────────────────────────────────────────────────────────────┐
│                      Redis Cluster                           │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │  Master 1  │  │  Master 2  │  │  Master 3  │            │
│  └──────┬─────┘  └──────┬─────┘  └──────┬─────┘            │
│         │                │                │                  │
│  ┌──────▼─────┐  ┌──────▼─────┐  ┌──────▼─────┐            │
│  │  Replica 1 │  │  Replica 2 │  │  Replica 3 │            │
│  └────────────┘  └────────────┘  └────────────┘            │
└─────────────────────────────────────────────────────────────┘
```

#### **Cached Data:**

1. **File Metadata Cache:**
   - Key: `file:metadata:{fileId}`
   - TTL: 1 hour
   - Data: File metadata JSON

2. **User Session Cache:**
   - Key: `session:{sessionId}`
   - TTL: 24 hours
   - Data: User session data

3. **Permission Cache:**
   - Key: `permission:{userId}:{resourceId}`
   - TTL: 15 minutes
   - Data: Permission list

4. **Small File Content Cache:**
   - Key: `file:content:{fileId}`
   - TTL: 30 minutes
   - Data: File binary (< 1MB)

5. **Search Result Cache:**
   - Key: `search:{queryHash}`
   - TTL: 5 minutes
   - Data: Search results

#### **Cache Invalidation Strategy:**

- **Write-Through:** Update cache on every write
- **Cache-Aside:** Load from DB on cache miss
- **Event-Based Invalidation:** Invalidate on domain events (FileUpdated, FileDeleted)

---

## 3. Communication & Messaging

### 3.1 Message Queue Architecture

**Technology:** RabbitMQ or Apache Kafka

#### **Use Cases:**

1. **Asynchronous File Processing:**
   - Virus scanning
   - Thumbnail generation
   - Video transcoding
   - Document indexing for search

2. **Event-Driven Architecture:**
   - FileUploadedEvent
   - FileDeletedEvent
   - FileSharedEvent
   - UserRegisteredEvent

3. **Notification Delivery:**
   - Email notifications
   - Push notifications
   - Webhook callbacks

#### **Message Queue Topology:**

```
┌─────────────────────────────────────────────────────────────┐
│                      Message Broker                          │
│                    (RabbitMQ/Kafka)                          │
└────────┬────────────┬────────────┬────────────┬─────────────┘
         │            │            │            │
    ┌────▼────┐  ┌───▼────┐  ┌───▼────┐  ┌────▼─────┐
    │ Virus   │  │Thumbnail│  │ Search │  │Notification│
    │ Scanner │  │Generator│  │Indexer │  │  Service  │
    └─────────┘  └─────────┘  └────────┘  └──────────┘
```

---

## 4. Security Architecture

### 4.1 Authentication & Authorization

#### **Authentication Flow:**

```
1. User Login → Auth Service
2. Validate Credentials
3. Generate JWT Token (Access + Refresh)
4. Return Tokens to Client
5. Client includes Access Token in API requests
6. API Gateway validates token
7. Forward request to service with user context
```

#### **JWT Token Structure:**

```json
{
  "sub": "user-uuid",
  "username": "john.doe",
  "email": "john@example.com",
  "roles": ["USER", "ADMIN"],
  "iat": 1640000000,
  "exp": 1640003600
}
```

#### **Authorization Model:**

**Role-Based Access Control (RBAC):**
- **ADMIN:** Full system access
- **USER:** Standard user permissions
- **GUEST:** Read-only access

**Resource-Based Permissions:**
- **OWNER:** Full control over resource
- **EDITOR:** Read + Write
- **VIEWER:** Read-only

### 4.2 Data Security

#### **Encryption:**

1. **Data at Rest:**
   - AES-256 encryption for stored files
   - Encrypted database connections
   - Encrypted backups

2. **Data in Transit:**
   - TLS 1.3 for all API communications
   - HTTPS only
   - Certificate pinning for mobile apps

3. **Key Management:**
   - AWS KMS or HashiCorp Vault
   - Key rotation policy (90 days)
   - Separate keys per tenant (for multi-tenancy)

#### **Security Best Practices:**

- Input validation and sanitization
- SQL injection prevention (parameterized queries)
- XSS protection
- CSRF tokens
- Rate limiting
- DDoS protection
- Regular security audits
- Penetration testing

---

## 5. Scalability & Performance

### 5.1 Horizontal Scaling

#### **Stateless Services:**
All application services are stateless and can be scaled horizontally:

```
┌──────────────────────────────────────────────────────────┐
│                    Load Balancer                          │
└────┬────────┬────────┬────────┬────────┬────────┬────────┘
     │        │        │        │        │        │
┌────▼───┐ ┌─▼────┐ ┌─▼────┐ ┌─▼────┐ ┌─▼────┐ ┌─▼────┐
│Service │ │Service│ │Service│ │Service│ │Service│ │Service│
│ Pod 1  │ │ Pod 2 │ │ Pod 3 │ │ Pod 4 │ │ Pod 5 │ │ Pod N │
└────────┘ └───────┘ └───────┘ └───────┘ └───────┘ └───────┘
```

#### **Database Scaling:**

1. **Read Replicas:**
   - Master-Slave replication
   - Read queries → Replicas
   - Write queries → Master

2. **Sharding:**
   - Shard by user_id
   - Consistent hashing for distribution

3. **Connection Pooling:**
   - HikariCP for efficient connection management

### 5.2 Performance Optimization

#### **CDN Integration:**
- CloudFlare or AWS CloudFront
- Cache static assets
- Edge caching for frequently accessed files

#### **Lazy Loading:**
- Pagination for file lists
- Infinite scroll
- Thumbnail loading before full image

#### **Compression:**
- Gzip/Brotli for API responses
- Image optimization (WebP format)
- Video streaming (HLS/DASH)

#### **Async Processing:**
- Non-blocking I/O
- CompletableFuture for async operations
- Reactive programming (Project Reactor)

---

## 6. Monitoring & Observability

### 6.1 Logging

**Centralized Logging:** ELK Stack (Elasticsearch, Logstash, Kibana)

**Log Levels:**
- ERROR: System errors
- WARN: Potential issues
- INFO: Important events
- DEBUG: Detailed debugging

**Structured Logging:**
```json
{
  "timestamp": "2025-12-21T23:57:00Z",
  "level": "INFO",
  "service": "file-service",
  "traceId": "abc123",
  "userId": "user-uuid",
  "action": "FILE_UPLOAD",
  "fileId": "file-uuid",
  "duration": 1250,
  "status": "SUCCESS"
}
```

### 6.2 Metrics

**Monitoring Stack:** Prometheus + Grafana

**Key Metrics:**
- Request rate (requests/sec)
- Error rate (%)
- Response time (p50, p95, p99)
- CPU/Memory usage
- Database connection pool
- Cache hit ratio
- Storage node capacity
- Queue depth

### 6.3 Distributed Tracing

**Technology:** Jaeger or Zipkin

**Trace Context Propagation:**
- Trace ID passed through all services
- Span creation for each operation
- End-to-end request visualization

### 6.4 Alerting

**Alert Manager:** PagerDuty or Opsgenie

**Alert Rules:**
- Service down (5xx errors > threshold)
- High latency (p99 > 5s)
- Storage capacity > 80%
- Database connection pool exhausted
- Queue backlog > threshold

---

## 7. Disaster Recovery & High Availability

### 7.1 Backup Strategy

**Database Backups:**
- Full backup: Daily
- Incremental backup: Hourly
- Point-in-time recovery (PITR)
- Backup retention: 30 days

**File Storage Backups:**
- Cross-region replication
- Versioning enabled
- Soft delete (30-day retention)

### 7.2 High Availability

**Target SLA:** 99.9% uptime (8.76 hours downtime/year)

**HA Architecture:**
- Multi-AZ deployment
- Auto-scaling groups
- Health checks and auto-recovery
- Database failover (< 30 seconds)
- Zero-downtime deployments (blue-green)

### 7.3 Disaster Recovery

**RTO (Recovery Time Objective):** 1 hour
**RPO (Recovery Point Objective):** 15 minutes

**DR Plan:**
1. Automated failover to secondary region
2. Database restore from backup
3. DNS failover
4. Service health verification
5. Monitoring and alerting

---

## 8. Deployment Architecture

### 8.1 Container Orchestration

**Technology:** Kubernetes (K8s)

```
┌─────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                        │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                   Namespace: production                 │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │ │
│  │  │   Deployment │  │   Deployment │  │   Deployment │ │ │
│  │  │  File Service│  │  User Service│  │Search Service│ │ │
│  │  │   (3 pods)   │  │   (2 pods)   │  │   (2 pods)   │ │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘ │ │
│  │  ┌──────────────┐  ┌──────────────┐                   │ │
│  │  │   Service    │  │   Ingress    │                   │ │
│  │  │  (ClusterIP) │  │  Controller  │                   │ │
│  │  └──────────────┘  └──────────────┘                   │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 8.2 CI/CD Pipeline

**Tools:** Jenkins, GitLab CI, or GitHub Actions

**Pipeline Stages:**
1. Code checkout
2. Unit tests
3. Integration tests
4. Build Docker image
5. Push to registry
6. Deploy to staging
7. Automated testing
8. Deploy to production (with approval)

---

## 9. Current Implementation Mapping

Based on your existing codebase, here's how it maps to the HLD:

### **Domain Layer:**
- ✅ `File` domain model exists
- ⚠️ Missing: `Folder`, `User`, `Permission`, `Share` domain models
- ⚠️ Missing: Domain services

### **Application Layer:**
- ✅ `FileUseCase` interface (port)
- ✅ `FileService` implementation
- ⚠️ Missing: Other use cases (User, Folder, Search, Share)

### **Infrastructure Layer:**
- ✅ `FileController` with basic upload endpoint
- ✅ `UserController` skeleton
- ✅ Entity classes with JPA annotations
- ✅ `BaseEntity` with audit fields
- ⚠️ Missing: Repository implementations
- ⚠️ Missing: Storage adapters
- ⚠️ Missing: Cache adapters

### **Configuration:**
- ✅ PostgreSQL datasource configured
- ✅ JPA/Hibernate configured
- ⚠️ Missing: Redis configuration
- ⚠️ Missing: Message queue configuration
- ⚠️ Missing: Security configuration

---

## 10. Recommended Next Steps

### **Phase 1: Core Functionality (Weeks 1-4)**
1. Implement complete domain models
2. Implement repository adapters
3. Implement file upload/download with storage adapter
4. Implement user authentication
5. Add basic API endpoints

### **Phase 2: Advanced Features (Weeks 5-8)**
1. Implement folder management
2. Implement file versioning
3. Implement search functionality
4. Add sharing and permissions
5. Implement caching layer

### **Phase 3: Scalability & Production (Weeks 9-12)**
1. Add message queue integration
2. Implement monitoring and logging
3. Add rate limiting and security hardening
4. Performance testing and optimization
5. Documentation and deployment automation

---

## 11. Technology Stack Summary

| Component | Technology |
|-----------|-----------|
| **Language** | Java 25 |
| **Framework** | Spring Boot 4.0 |
| **Database** | PostgreSQL |
| **Cache** | Redis |
| **Message Queue** | RabbitMQ / Kafka |
| **Search** | Elasticsearch |
| **Storage** | MinIO / S3 |
| **Container** | Docker |
| **Orchestration** | Kubernetes |
| **Monitoring** | Prometheus + Grafana |
| **Logging** | ELK Stack |
| **Tracing** | Jaeger |
| **API Gateway** | Kong / NGINX |
| **Load Balancer** | NGINX / HAProxy |

---

## Conclusion

This High Level Design provides a comprehensive blueprint for building a scalable, secure, and maintainable file storage system using Hexagonal Architecture with DDD principles. The architecture ensures:

- **Separation of Concerns:** Clear boundaries between domain, application, and infrastructure
- **Testability:** Easy to test with ports and adapters pattern
- **Scalability:** Horizontal scaling of stateless services
- **Maintainability:** Clean code structure following DDD principles
- **Extensibility:** Easy to add new features without affecting existing code

The design supports millions of users and petabytes of data while maintaining high availability and performance.
