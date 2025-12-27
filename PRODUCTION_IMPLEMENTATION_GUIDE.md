# Production File Storage Backend - Implementation Guide

## ✅ Multi-Instance Ready Architecture

This backend is designed for **horizontal scalability** with multiple instances behind a load balancer.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Load Balancer                         │
│                   (NGINX / AWS ALB)                      │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐        ┌──────▼────────┐
│  Instance 1    │        │  Instance 2    │  (Stateless)
│  Spring Boot   │        │  Spring Boot   │
└───────┬────────┘        └──────┬─────────┘
        │                         │
        └────────────┬────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐        ┌──────▼────────┐
│  PostgreSQL    │        │  Redis Cache  │
│  (Metadata +   │        │  (Optional)   │
│   Encryption   │        │               │
│   Keys)        │        │               │
└────────────────┘        └───────────────┘
        │
┌───────▼─────────────────────────────────┐
│     Storage Backends (Registered in DB) │
│  ┌──────────┐  ┌──────────┐  ┌────────┐│
│  │  MinIO   │  │  AWS S3  │  │  SFTP  ││
│  │  Node 1  │  │  Node 2  │  │ Node 3 ││
│  └──────────┘  └──────────┘  └────────┘│
└─────────────────────────────────────────┘
```

---

## 🔧 Fixed Issues

### **1. Storage Router Bug** ✅
**Fixed:** `FileStoragePortRouter` now correctly routes to the selected adapter instead of always using SFTP.

### **2. In-Memory Key Vault** ✅
**Fixed:** Replaced `AesEncryptionAdapter` with `DatabaseEncryptionAdapter` that stores encryption keys in PostgreSQL.

**Multi-Instance Safe:** All instances can now access encryption keys from the shared database.

### **3. Round-Robin Strategy** ✅
**Removed:** `RoundRobinNodeSelectionStrategy` (not multi-instance safe).

**Replaced with:** Least-used capacity strategy inlined in `StorageSelectionService`.

---

## 🗑️ Removed Abstractions

### **Deleted Files:**
- ❌ `NodeSelectionStrategy` interface (unnecessary abstraction)
- ❌ `RoundRobinNodeSelectionStrategy` (not multi-instance safe)
- ❌ `LeastUsedNodeSelectionStrategy` (logic inlined)
- ❌ `CachePort` interface (unused)

### **Simplified:**
- ✅ `StorageSelectionService` - Direct implementation without strategy pattern
- ✅ Configuration - Removed unnecessary beans

---

## 📦 Essential Components

### **Domain Layer**
```
domain/
├── model/
│   └── storage/
│       ├── FileAggregate.java          (Aggregate Root)
│       ├── FileMetadata.java           (Entity)
│       ├── StorageReference.java       (Value Object - Encrypted)
│       ├── FileChecksum.java           (Value Object)
│       ├── StorageNode.java            (Value Object)
│       ├── FileId.java                 (Value Object)
│       ├── EncryptedData.java          (Value Object)
│       └── EncryptionKey.java          (Value Object)
└── service/
    ├── StorageSelectionService.java    (Domain Service)
    └── MetadataEncryptionService.java  (Domain Service)
```

### **Application Layer**
```
application/
├── port/
│   ├── in/
│   │   └── storage/
│   │       ├── UploadFileUseCase.java
│   │       ├── DownloadFileUseCase.java
│   │       ├── FileUploadCommand.java
│   │       └── FileDownloadQuery.java
│   └── out/
│       └── storage/
│           ├── FileStoragePort.java
│           ├── FileMetadataRepositoryPort.java
│           ├── EncryptionPort.java
│           └── StorageNodeRegistryPort.java
└── service/
    └── storage/
        ├── FileUploadService.java
        └── FileDownloadService.java
```

### **Infrastructure Layer**
```
infrastructure/
├── web/
│   └── controller/
│       └── ProductionFileController.java
├── persistence/
│   ├── entity/
│   │   └── storage/
│   │       ├── FileMetadataEntity.java
│   │       ├── StorageNodeEntity.java
│   │       └── EncryptionKeyEntity.java
│   ├── repository/
│   │   └── storage/
│   │       ├── FileMetadataJpaRepository.java
│   │       ├── StorageNodeJpaRepository.java
│   │       └── EncryptionKeyJpaRepository.java
│   └── adapter/
│       └── storage/
│           ├── FileMetadataRepositoryAdapter.java
│           └── StorageNodeRegistryAdapter.java
├── storage/
│   └── adapter/
│       ├── FileStoragePortRouter.java
│       ├── MinIOStorageAdapter.java
│       ├── S3StorageAdapter.java
│       └── SFTPStorageAdapter.java
├── security/
│   └── DatabaseEncryptionAdapter.java
└── config/
    ├── StorageConfiguration.java
    └── SecurityConfig.java
```

---

## 🚀 Deployment Guide

### **1. Database Setup**

Run the migration script:
```sql
-- Located at: src/main/resources/db/migration/V1__initial_schema.sql
```

Or use Flyway/Liquibase for automatic migrations.

### **2. Environment Variables**

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/file_storage
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

# Encryption (CRITICAL: Change in production)
ENCRYPTION_MASTER_KEY=<base64-encoded-32-byte-key>

# MinIO
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# AWS S3 (if using)
AWS_REGION=us-east-1
AWS_ACCESS_KEY=your_access_key
AWS_SECRET_KEY=your_secret_key

# Redis (optional)
SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379
```

### **3. Generate Master Encryption Key**

```bash
# Generate a secure 256-bit key
openssl rand -base64 32
```

**IMPORTANT:** Store this key securely (AWS Secrets Manager, HashiCorp Vault, etc.)

### **4. Register Storage Nodes**

Insert storage nodes into the database:

```sql
-- MinIO Node
INSERT INTO storage_nodes (
    node_id, storage_type, node_url, 
    total_capacity_gb, used_capacity_gb, file_count,
    status, created_at, updated_at
) VALUES (
    'minio-node-1', 'MINIO', 'http://minio:9000',
    1000, 0, 0,
    'ACTIVE', NOW(), NOW()
);

-- AWS S3 Node
INSERT INTO storage_nodes (
    node_id, storage_type, node_url, 
    total_capacity_gb, status, created_at, updated_at
) VALUES (
    's3-us-east-1', 'S3', 'https://s3.us-east-1.amazonaws.com',
    NULL, 'ACTIVE', NOW(), NOW()
);
```

### **5. Docker Deployment**

```yaml
# docker-compose.yml
version: '3.8'

services:
  app-1:
    image: file-storage:latest
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/file_storage
      - ENCRYPTION_MASTER_KEY=${ENCRYPTION_MASTER_KEY}
    depends_on:
      - postgres
      - redis
      - minio
    ports:
      - "8081:8080"

  app-2:
    image: file-storage:latest
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/file_storage
      - ENCRYPTION_MASTER_KEY=${ENCRYPTION_MASTER_KEY}
    depends_on:
      - postgres
      - redis
      - minio
    ports:
      - "8082:8080"

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - app-1
      - app-2

  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: file_storage
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data

volumes:
  postgres_data:
  minio_data:
```

---

## 🔐 Security Recommendations

### **Production Encryption**

Replace `DatabaseEncryptionAdapter` with cloud-native key management:

#### **AWS KMS Example:**
```java
@Component
@Primary
public class AwsKmsEncryptionAdapter implements EncryptionPort {
    
    private final KmsClient kmsClient;
    
    @Override
    public EncryptedData encrypt(String plaintext, EncryptionKey key) {
        EncryptRequest request = EncryptRequest.builder()
            .keyId(key.keyRef())
            .plaintext(SdkBytes.fromUtf8String(plaintext))
            .build();
            
        EncryptResponse response = kmsClient.encrypt(request);
        return new EncryptedData("AWS-KMS", new byte[0], response.ciphertextBlob().asByteArray());
    }
}
```

#### **HashiCorp Vault Example:**
```java
@Component
@Primary
public class VaultEncryptionAdapter implements EncryptionPort {
    
    private final VaultTemplate vaultTemplate;
    
    @Override
    public EncryptedData encrypt(String plaintext, EncryptionKey key) {
        VaultTransitContext context = VaultTransitContext.builder()
            .plaintext(plaintext.getBytes())
            .build();
            
        Ciphertext ciphertext = vaultTemplate.opsForTransit()
            .encrypt(key.keyRef(), context);
            
        return new EncryptedData("VAULT", new byte[0], ciphertext.getCiphertext().getBytes());
    }
}
```

---

## 📊 Monitoring & Observability

### **Health Checks**

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check storage node status
curl http://localhost:8080/actuator/health/storage-nodes
```

### **Metrics**

Enable Prometheus metrics:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### **Key Metrics to Monitor:**
- Upload/download throughput
- Storage node capacity
- Encryption/decryption latency
- Database connection pool usage
- Cache hit ratio

---

## 🧪 Testing Multi-Instance Deployment

### **Test Upload from Instance 1, Download from Instance 2:**

```bash
# Upload file to Instance 1
curl -X POST http://localhost:8081/api/v1/storage/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.pdf"

# Response: {"fileId": "abc-123", ...}

# Download from Instance 2
curl -X GET http://localhost:8082/api/v1/storage/download/abc-123 \
  -H "Authorization: Bearer $TOKEN" \
  -o downloaded.pdf

# Verify checksum matches
sha256sum test.pdf downloaded.pdf
```

---

## 🎯 Production Checklist

- [ ] Replace in-memory encryption with AWS KMS/Vault
- [ ] Configure proper master encryption key
- [ ] Set up database backups
- [ ] Configure Redis for caching (optional)
- [ ] Set up monitoring (Prometheus + Grafana)
- [ ] Configure log aggregation (ELK/Splunk)
- [ ] Set up alerts for storage capacity
- [ ] Implement rate limiting
- [ ] Configure CORS policies
- [ ] Set up SSL/TLS certificates
- [ ] Configure database connection pooling
- [ ] Set up automated health checks
- [ ] Implement circuit breakers for storage backends
- [ ] Configure backup storage nodes
- [ ] Set up disaster recovery plan

---

## 📈 Scaling Strategy

### **Horizontal Scaling:**
1. Add more backend instances (stateless)
2. Add more storage nodes (register in database)
3. Scale PostgreSQL (read replicas)
4. Scale Redis (cluster mode)

### **Storage Node Management:**
```sql
-- Add new storage node
INSERT INTO storage_nodes (
    node_id, storage_type, node_url, 
    total_capacity_gb, status, created_at, updated_at
) VALUES (
    'minio-node-2', 'MINIO', 'http://minio-2:9000',
    2000, 'ACTIVE', NOW(), NOW()
);

-- Mark node as maintenance
UPDATE storage_nodes 
SET status = 'MAINTENANCE', updated_at = NOW()
WHERE node_id = 'minio-node-1';

-- Decommission node (after migrating files)
UPDATE storage_nodes 
SET status = 'OFFLINE', updated_at = NOW()
WHERE node_id = 'old-node';
```

---

## 🔄 Migration from Old Schema

If you have existing data in the old schema (`files` table), run this migration:

```sql
-- Migrate from old schema to new schema
INSERT INTO file_metadata (
    id, file_name, content_type, file_size,
    checksum_algorithm, checksum_hash,
    storage_type, storage_node_id, encrypted_path, encryption_key_ref,
    owner_id, uploaded_at, updated_at, status
)
SELECT 
    id, name, content_type, size,
    'SHA-256', checksum,
    'MINIO', 'default-node', storage_location, 'migration-key',
    owner_id, created_at, updated_at, status
FROM files
WHERE status = 'ACTIVE';
```

**Note:** You'll need to re-encrypt storage paths with the new encryption service.

---

## 🆘 Troubleshooting

### **Issue: Download fails with "Encryption key not found"**
**Cause:** Key was generated on Instance A but Instance B can't access it.
**Solution:** Ensure `DatabaseEncryptionAdapter` is active (has `@Primary` annotation).

### **Issue: Files always uploaded to same node**
**Cause:** Node capacity not being updated.
**Solution:** Check `StorageNodeRegistryAdapter.updateNodeCapacity()` is being called.

### **Issue: Storage router always uses SFTP**
**Cause:** Bug in router (now fixed).
**Solution:** Ensure you're using the updated `FileStoragePortRouter.java`.

---

## 📚 Additional Resources

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [AWS KMS Best Practices](https://docs.aws.amazon.com/kms/latest/developerguide/best-practices.html)
- [Spring Boot Production Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)

---

**Built with Spring Boot 4 + Java 25 for Production-Grade Multi-Instance Deployment**
