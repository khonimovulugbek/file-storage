# Production File Storage System - Architecture Design

## 🎯 System Overview

A horizontally scalable file storage system supporting multiple storage backends (MinIO, AWS S3, SFTP) with secure metadata management, designed for production environments handling millions of files.

---

## 🏗️ High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│              (REST Controllers - Adapters)                   │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                  Application Layer                           │
│        (Use Cases - Upload/Download Orchestration)           │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                    Domain Layer                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  FileAggregate (Root)                                │   │
│  │  - FileMetadata (Entity)                             │   │
│  │  - StorageReference (Value Object - ENCRYPTED)       │   │
│  │  - FileChecksum (Value Object)                       │   │
│  │  - StorageNode (Value Object)                        │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Domain Services                                      │   │
│  │  - StorageSelectionService (Strategy Pattern)        │   │
│  │  - MetadataEncryptionService                         │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                    Ports (Interfaces)                        │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ Outbound Ports:                                     │    │
│  │ - FileStoragePort                                   │    │
│  │ - FileMetadataPort                                  │    │
│  │ - EncryptionPort                                    │    │
│  │ - StorageNodeRegistryPort                           │    │
│  └─────────────────────────────────────────────────────┘    │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│              Infrastructure Layer (Adapters)                 │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  MinIO Adapter   │  │  S3 Adapter  │  │ SFTP Adapter │  │
│  └──────────────────┘  └──────────────┘  └──────────────┘  │
│                                                              │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ PostgreSQL       │  │ AES-256      │  │ Redis Cache  │  │
│  │ Metadata Repo    │  │ Encryption   │  │ Adapter      │  │
│  └──────────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Domain Model (DDD)

### **1. FileAggregate (Aggregate Root)**

```java
FileAggregate
├── FileId (Identity - Value Object)
├── FileMetadata (Entity)
│   ├── FileName
│   ├── ContentType
│   ├── FileSize
│   ├── UploadedAt
│   └── OwnerId
├── StorageReference (Value Object - ENCRYPTED)
│   ├── StorageType (MINIO, S3, SFTP)
│   ├── StorageNodeId (which server/node)
│   ├── EncryptedPath (AES-256 encrypted)
│   ├── Bucket/Container
│   └── Region (for S3)
├── FileChecksum (Value Object)
│   ├── Algorithm (SHA-256)
│   └── Hash
└── StorageNode (Value Object)
    ├── NodeId
    ├── NodeUrl
    ├── Capacity
    └── Status (ACTIVE, FULL, MAINTENANCE)
```

### **2. Key Value Objects**

#### **StorageReference** (Encrypted)
- **Purpose**: Securely store file location without exposing raw paths
- **Encryption**: AES-256-GCM with per-file encryption key
- **Structure**:
  ```json
  {
    "storageType": "MINIO",
    "nodeId": "minio-node-1",
    "encryptedPath": "AES256(bucket/path/to/file.pdf)",
    "bucket": "files-2024",
    "encryptionKeyRef": "key-id-12345"
  }
  ```

#### **FileChecksum**
- **Purpose**: Verify file integrity
- **Algorithm**: SHA-256
- **Used for**: Deduplication, integrity verification

#### **StorageNode**
- **Purpose**: Track available storage servers
- **Attributes**:
  - `nodeId`: Unique identifier
  - `nodeUrl`: Connection endpoint
  - `capacity`: Total/used storage
  - `status`: Operational status
  - `storageType`: MINIO, S3, SFTP

---

## 🔌 Ports (Interfaces)

### **Outbound Ports**

```java
// Storage abstraction
public interface FileStoragePort {
    StorageResult store(InputStream content, StorageContext context);
    InputStream retrieve(StorageReference reference);
    void delete(StorageReference reference);
    boolean exists(StorageReference reference);
}

// Metadata persistence
public interface FileMetadataPort {
    FileAggregate save(FileAggregate aggregate);
    Optional<FileAggregate> findById(FileId fileId);
    Optional<FileAggregate> findByChecksum(FileChecksum checksum);
    void delete(FileId fileId);
}

// Encryption for paths
public interface EncryptionPort {
    EncryptedData encrypt(String plaintext, EncryptionKey key);
    String decrypt(EncryptedData ciphertext, EncryptionKey key);
    EncryptionKey generateKey();
}

// Storage node registry
public interface StorageNodeRegistryPort {
    List<StorageNode> findAvailableNodes(StorageType type);
    StorageNode findById(String nodeId);
    void registerNode(StorageNode node);
    void updateNodeStatus(String nodeId, NodeStatus status);
}
```

### **Inbound Ports (Use Cases)**

```java
public interface UploadFileUseCase {
    FileUploadResult upload(FileUploadCommand command);
}

public interface DownloadFileUseCase {
    FileDownloadResult download(FileDownloadQuery query);
}
```

---

## 🔐 Security Strategy

### **1. Metadata Protection**

**Problem**: Database breach should NOT allow unauthorized file access.

**Solution**: Multi-layer security

#### **Layer 1: Path Encryption**
```java
// Raw path NEVER stored in database
String rawPath = "minio-node-1/bucket-2024/user-123/document.pdf";

// Encrypted before storage
EncryptedPath encrypted = encryptionService.encrypt(rawPath, fileKey);
// Stored: "AES256:IV:ciphertext"
```

#### **Layer 2: Encryption Key Management**
- **Per-file encryption keys** stored in separate key vault (AWS KMS, HashiCorp Vault)
- Database only stores **key reference ID**, not the actual key
- Keys rotated periodically

#### **Layer 3: Signed Download Tokens**
```java
// Generate time-limited signed token
DownloadToken token = tokenService.generateToken(
    fileId, 
    userId, 
    expiresIn = 5.minutes
);

// Token contains:
// - fileId (encrypted)
// - userId (encrypted)
// - expiry timestamp
// - HMAC signature
```

### **2. Authorization Flow**

```
User Request → JWT Auth → Permission Check → Token Generation → Download
                  ↓            ↓                    ↓
              Valid User   Owns File         Signed Token
                                                    ↓
                                          Decrypt Path → Stream File
```

---

## 🗄️ Database Schema

### **file_metadata** (Main table)
```sql
CREATE TABLE file_metadata (
    id                  UUID PRIMARY KEY,
    file_name           VARCHAR(255) NOT NULL,
    content_type        VARCHAR(100) NOT NULL,
    file_size           BIGINT NOT NULL,
    checksum_algorithm  VARCHAR(20) NOT NULL,
    checksum_hash       VARCHAR(128) NOT NULL,
    
    -- Storage reference (ENCRYPTED)
    storage_type        VARCHAR(20) NOT NULL,  -- MINIO, S3, SFTP
    storage_node_id     VARCHAR(50) NOT NULL,
    encrypted_path      TEXT NOT NULL,         -- AES-256 encrypted
    encryption_key_ref  VARCHAR(100) NOT NULL, -- Reference to key vault
    bucket_name         VARCHAR(100),
    region              VARCHAR(50),
    
    -- Metadata
    owner_id            UUID NOT NULL,
    uploaded_at         TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    status              VARCHAR(20) NOT NULL,  -- ACTIVE, DELETED
    
    -- Indexes
    CONSTRAINT uk_checksum UNIQUE (checksum_hash),
    INDEX idx_owner (owner_id),
    INDEX idx_storage_node (storage_node_id),
    INDEX idx_uploaded_at (uploaded_at)
);
```

### **storage_nodes** (Node registry)
```sql
CREATE TABLE storage_nodes (
    node_id             VARCHAR(50) PRIMARY KEY,
    storage_type        VARCHAR(20) NOT NULL,
    node_url            VARCHAR(500) NOT NULL,
    access_key          VARCHAR(255),          -- Encrypted
    secret_key          TEXT,                  -- Encrypted
    
    -- Capacity tracking
    total_capacity_gb   BIGINT,
    used_capacity_gb    BIGINT,
    file_count          BIGINT DEFAULT 0,
    
    -- Status
    status              VARCHAR(20) NOT NULL,  -- ACTIVE, FULL, MAINTENANCE
    health_check_url    VARCHAR(500),
    last_health_check   TIMESTAMP,
    
    -- Metadata
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    
    INDEX idx_storage_type_status (storage_type, status)
);
```

### **encryption_keys** (Key vault reference)
```sql
CREATE TABLE encryption_keys (
    key_ref             VARCHAR(100) PRIMARY KEY,
    key_vault_id        VARCHAR(255) NOT NULL,  -- External vault reference
    algorithm           VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    rotated_at          TIMESTAMP,
    status              VARCHAR(20) NOT NULL    -- ACTIVE, ROTATED, REVOKED
);
```

---

## 📤 Upload Flow (Step-by-Step)

```
1. Client Request
   ↓
2. Upload Controller (Adapter)
   - Validate file
   - Extract metadata
   ↓
3. UploadFileUseCase (Application)
   - Create FileUploadCommand
   ↓
4. Domain Service: StorageSelectionService
   - Query available storage nodes
   - Apply selection strategy (least used, round-robin, geo-location)
   - Select optimal node
   ↓
5. Domain Service: MetadataEncryptionService
   - Generate encryption key
   - Store key in vault
   - Get key reference
   ↓
6. FileStoragePort (via Strategy Pattern)
   - Route to appropriate adapter (MinIO/S3/SFTP)
   - Upload file to selected node
   - Get storage path
   ↓
7. Encrypt Storage Path
   - Encrypt path with file-specific key
   - Create StorageReference value object
   ↓
8. Create FileAggregate
   - FileMetadata
   - Encrypted StorageReference
   - FileChecksum (SHA-256)
   ↓
9. FileMetadataPort
   - Persist to database
   ↓
10. Return FileUploadResult
    - FileId
    - Checksum
    - Upload timestamp
```

---

## 📥 Download Flow (Step-by-Step)

```
1. Client Request (fileId + JWT)
   ↓
2. Download Controller (Adapter)
   - Validate JWT
   - Extract userId
   ↓
3. DownloadFileUseCase (Application)
   - Create FileDownloadQuery
   ↓
4. FileMetadataPort
   - Retrieve FileAggregate by fileId
   ↓
5. Authorization Check
   - Verify user owns file OR has permission
   ↓
6. Decrypt Storage Path
   - Get encryption key from vault (using key_ref)
   - Decrypt StorageReference.encryptedPath
   ↓
7. Resolve Storage Node
   - Get StorageNode by nodeId
   - Verify node is ACTIVE
   ↓
8. FileStoragePort (via Strategy)
   - Route to appropriate adapter
   - Retrieve file stream
   ↓
9. Stream Response
   - Set Content-Type, Content-Length
   - Stream file to client
   - Log download event
```

---

## 🔄 Horizontal Scalability Strategy

### **Problem**: Storage server becomes full

### **Solution**: Dynamic node registration

```java
// 1. Register new storage node
StorageNode newNode = StorageNode.builder()
    .nodeId("minio-node-2")
    .storageType(StorageType.MINIO)
    .nodeUrl("http://minio-2:9000")
    .totalCapacityGb(5000)
    .status(NodeStatus.ACTIVE)
    .build();

storageNodeRegistry.registerNode(newNode);

// 2. Upload service automatically uses new node
// Selection strategy picks node with most available space

// 3. Old files remain on old nodes
// Download service resolves correct node from metadata
```

### **Node Selection Strategies**

```java
public interface NodeSelectionStrategy {
    StorageNode selectNode(List<StorageNode> availableNodes);
}

// 1. Least Used Strategy
class LeastUsedStrategy implements NodeSelectionStrategy {
    public StorageNode selectNode(List<StorageNode> nodes) {
        return nodes.stream()
            .filter(n -> n.getStatus() == ACTIVE)
            .min(Comparator.comparing(StorageNode::getUsedCapacityPercent))
            .orElseThrow();
    }
}

// 2. Round Robin Strategy
class RoundRobinStrategy implements NodeSelectionStrategy {
    private AtomicInteger counter = new AtomicInteger(0);
    
    public StorageNode selectNode(List<StorageNode> nodes) {
        int index = counter.getAndIncrement() % nodes.size();
        return nodes.get(index);
    }
}

// 3. Geo-Location Strategy
class GeoLocationStrategy implements NodeSelectionStrategy {
    public StorageNode selectNode(List<StorageNode> nodes) {
        String userRegion = getUserRegion();
        return nodes.stream()
            .filter(n -> n.getRegion().equals(userRegion))
            .findFirst()
            .orElse(nodes.get(0));
    }
}
```

---

## 🔌 Storage Adapter Implementation

### **Common Interface**

```java
public interface FileStoragePort {
    StorageResult store(InputStream content, StorageContext context);
    InputStream retrieve(StorageReference reference);
    void delete(StorageReference reference);
    boolean exists(StorageReference reference);
}

public class StorageContext {
    private String fileName;
    private String contentType;
    private long fileSize;
    private String bucket;
    private StorageNode targetNode;
}

public class StorageResult {
    private String absolutePath;
    private String bucket;
    private String etag;
    private long uploadedBytes;
}
```

### **Adapter Routing (Strategy Pattern)**

```java
@Component
public class FileStoragePortRouter implements FileStoragePort {
    
    private final Map<StorageType, FileStoragePort> adapters;
    
    public FileStoragePortRouter(
            MinIOStorageAdapter minioAdapter,
            S3StorageAdapter s3Adapter,
            SFTPStorageAdapter sftpAdapter) {
        
        this.adapters = Map.of(
            StorageType.MINIO, minioAdapter,
            StorageType.S3, s3Adapter,
            StorageType.SFTP, sftpAdapter
        );
    }
    
    @Override
    public StorageResult store(InputStream content, StorageContext context) {
        StorageType type = context.getTargetNode().getStorageType();
        FileStoragePort adapter = adapters.get(type);
        return adapter.store(content, context);
    }
    
    @Override
    public InputStream retrieve(StorageReference reference) {
        FileStoragePort adapter = adapters.get(reference.getStorageType());
        return adapter.retrieve(reference);
    }
}
```

---

## 🎯 Key Design Decisions

### **1. Why Encrypt Storage Paths?**
- **Security**: Database breach doesn't expose file locations
- **Compliance**: GDPR, HIPAA require data protection
- **Indirection**: Change storage without updating all references

### **2. Why Per-File Encryption Keys?**
- **Isolation**: Compromised key affects only one file
- **Key Rotation**: Rotate keys without re-encrypting all files
- **Audit**: Track key usage per file

### **3. Why Separate Node Registry?**
- **Dynamic Scaling**: Add/remove nodes without code changes
- **Health Monitoring**: Track node status in real-time
- **Load Balancing**: Intelligent node selection

### **4. Why Checksum Storage?**
- **Deduplication**: Avoid storing duplicate files
- **Integrity**: Verify file hasn't been corrupted
- **Caching**: Use checksum as cache key

---

## 📊 Performance Optimizations

### **1. Download Path Resolution**

**Slow Approach** (Multiple DB queries):
```sql
SELECT * FROM file_metadata WHERE id = ?;
SELECT * FROM storage_nodes WHERE node_id = ?;
SELECT * FROM encryption_keys WHERE key_ref = ?;
```

**Fast Approach** (Single query with joins):
```sql
SELECT 
    fm.*,
    sn.node_url, sn.storage_type,
    ek.key_vault_id
FROM file_metadata fm
JOIN storage_nodes sn ON fm.storage_node_id = sn.node_id
JOIN encryption_keys ek ON fm.encryption_key_ref = ek.key_ref
WHERE fm.id = ?;
```

### **2. Caching Strategy**

```java
// Cache decrypted paths (short TTL)
@Cacheable(value = "storage-paths", key = "#fileId")
public String resolveStoragePath(UUID fileId) {
    // Expensive: DB query + decryption
}

// Cache storage node configs
@Cacheable(value = "storage-nodes", key = "#nodeId")
public StorageNode getStorageNode(String nodeId) {
    // Expensive: DB query
}
```

### **3. Presigned URLs (for S3/MinIO)**

```java
// Generate presigned URL (avoid streaming through app)
public String generatePresignedDownloadUrl(FileId fileId) {
    FileAggregate file = metadataPort.findById(fileId);
    StorageReference ref = file.getStorageReference();
    
    // Decrypt path
    String path = decrypt(ref.getEncryptedPath());
    
    // Generate presigned URL (valid for 5 minutes)
    return storageAdapter.generatePresignedUrl(path, Duration.ofMinutes(5));
}
```

---

## 🧪 Testing Strategy

### **Unit Tests**
- Domain models (value objects, aggregates)
- Domain services (encryption, selection strategy)
- Use cases (upload/download logic)

### **Integration Tests**
- Storage adapters (MinIO, S3, SFTP)
- Database repositories
- Encryption services

### **End-to-End Tests**
- Complete upload flow
- Complete download flow
- Node failover scenarios

---

## 🚀 Deployment Considerations

### **Storage Node Management**

```yaml
# Kubernetes ConfigMap
apiVersion: v1
kind: ConfigMap
metadata:
  name: storage-nodes
data:
  nodes.json: |
    [
      {
        "nodeId": "minio-node-1",
        "storageType": "MINIO",
        "nodeUrl": "http://minio-1:9000",
        "status": "ACTIVE"
      },
      {
        "nodeId": "s3-us-east-1",
        "storageType": "S3",
        "nodeUrl": "https://s3.us-east-1.amazonaws.com",
        "region": "us-east-1",
        "status": "ACTIVE"
      }
    ]
```

### **Key Vault Integration**

```java
// AWS KMS integration
@Component
public class AwsKmsEncryptionAdapter implements EncryptionPort {
    
    private final AWSKMS kmsClient;
    
    @Override
    public EncryptedData encrypt(String plaintext, EncryptionKey key) {
        EncryptRequest request = new EncryptRequest()
            .withKeyId(key.getKeyId())
            .withPlaintext(ByteBuffer.wrap(plaintext.getBytes()));
            
        EncryptResult result = kmsClient.encrypt(request);
        return new EncryptedData(result.getCiphertextBlob());
    }
}
```

---

This architecture provides:
✅ **Horizontal Scalability** - Add storage nodes dynamically
✅ **Security** - Encrypted paths, key vault integration
✅ **Pluggability** - Easy to add new storage types
✅ **Performance** - Optimized queries, caching, presigned URLs
✅ **Production-Ready** - Real-world concerns addressed
