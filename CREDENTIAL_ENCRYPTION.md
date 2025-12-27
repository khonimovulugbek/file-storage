# Credential Encryption for Storage Nodes

## Overview

Storage node credentials (access keys and secret keys) are automatically encrypted at rest in the database using AES-256-GCM encryption. This protects sensitive credentials from unauthorized access if the database is compromised.

## Architecture

### Components

1. **CredentialEncryptionService** (`domain/service/CredentialEncryptionService.java`)
   - Handles encryption/decryption of storage node credentials
   - Uses a dedicated encryption key for credential protection
   - Automatically manages key lifecycle

2. **StorageNodeMapper** (`infrastructure/persistence/mapper/storage/StorageNodeMapper.java`)
   - Automatically encrypts credentials when saving to database
   - Automatically decrypts credentials when loading from database
   - Transparent to application logic

3. **DatabaseEncryptionAdapter** (`infrastructure/security/DatabaseEncryptionAdapter.java`)
   - Primary encryption implementation using AES-256-GCM
   - Stores encryption keys in PostgreSQL
   - Uses master key to protect stored encryption keys

### Encryption Flow

```
User Input (Plain Credentials)
    ↓
StorageNodeController receives request
    ↓
StorageNode domain object created (plain credentials)
    ↓
StorageNodeMapper.toEntity() → Encrypts credentials
    ↓
StorageNodeEntity saved to database (encrypted credentials)
    ↓
Database stores: "AES-256-GCM:base64(iv):base64(ciphertext)"
```

### Decryption Flow

```
Database query retrieves StorageNodeEntity (encrypted credentials)
    ↓
StorageNodeMapper.toDomain() → Decrypts credentials
    ↓
StorageNode domain object (plain credentials)
    ↓
Storage adapters use plain credentials to connect to storage
```

## Configuration

### Master Key Setup

The master key is used to encrypt the credential encryption keys stored in the database.

**Development:**
```yaml
encryption:
  master-key: "YXNkZmFzZGZhc2RmYXNkZmFzZGZhc2RmYXNkZmFzZGY="
```

**Production:**

1. Generate a secure master key:
   ```bash
   openssl rand -base64 32
   ```

2. Store the key securely:
   - **AWS**: Use AWS Secrets Manager or Parameter Store
   - **Kubernetes**: Use Kubernetes Secrets
   - **HashiCorp Vault**: Store as a secret
   - **Azure**: Use Azure Key Vault

3. Configure via environment variable:
   ```bash
   export ENCRYPTION_MASTER_KEY="your-base64-encoded-key"
   ```

4. Update application.yaml:
   ```yaml
   encryption:
     master-key: ${ENCRYPTION_MASTER_KEY}
   ```

## Security Considerations

### Current Implementation (Development/Demo)

- ✅ Credentials encrypted at rest in database
- ✅ AES-256-GCM with authenticated encryption
- ✅ Unique IV per encryption operation
- ✅ Automatic key management
- ⚠️ Encryption keys stored in PostgreSQL
- ⚠️ Master key in configuration file

### Production Recommendations

1. **Use External Key Management Service**
   - Replace `DatabaseEncryptionAdapter` with AWS KMS, Azure Key Vault, or HashiCorp Vault
   - Implement key rotation policies
   - Enable audit logging for key access

2. **Secure Master Key Storage**
   - Never commit master key to version control
   - Use secrets management service
   - Rotate master key periodically
   - Implement key versioning

3. **Network Security**
   - Use TLS for all database connections
   - Encrypt database backups
   - Restrict database access with network policies

4. **Access Control**
   - Limit database user permissions
   - Enable database audit logging
   - Monitor credential access patterns

## Database Schema

Credentials are stored in the `storage_nodes` table:

```sql
CREATE TABLE storage_nodes (
    node_id VARCHAR(50) PRIMARY KEY,
    storage_type VARCHAR(20) NOT NULL,
    node_url VARCHAR(500) NOT NULL,
    access_key TEXT,  -- Encrypted: "AES-256-GCM:iv:ciphertext"
    secret_key TEXT,  -- Encrypted: "AES-256-GCM:iv:ciphertext"
    ...
);
```

Encryption keys are stored in the `encryption_keys` table:

```sql
CREATE TABLE encryption_keys (
    key_ref VARCHAR(255) PRIMARY KEY,
    encrypted_key TEXT NOT NULL,  -- Encrypted with master key
    algorithm VARCHAR(50) NOT NULL,
    vault_id VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

## Usage

### Registering a Storage Node

Credentials are automatically encrypted when registering a node:

```bash
curl -X POST http://localhost:8080/api/v1/storage/nodes \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": "minio-node-1",
    "storageType": "MINIO",
    "nodeUrl": "http://localhost:9000",
    "accessKey": "minioadmin",
    "secretKey": "minioadmin",
    "totalCapacityGb": 100,
    "status": "ACTIVE",
    "healthCheckUrl": "http://localhost:9000/minio/health/live"
  }'
```

The credentials are:
1. Received in plain text via API
2. Stored in `StorageNode` domain object (plain text)
3. Encrypted by `StorageNodeMapper` before database save
4. Stored in database as encrypted strings
5. Decrypted automatically when retrieved
6. Used in plain text by storage adapters

### Verifying Encryption

Check the database to verify credentials are encrypted:

```sql
SELECT node_id, access_key, secret_key 
FROM storage_nodes 
LIMIT 1;
```

You should see encrypted values like:
```
AES-256-GCM:dGVzdGl2MTIz:Y2lwaGVydGV4dGhlcmU=
```

## Key Rotation

To rotate the credential encryption key:

1. Generate a new encryption key (automatic on first use)
2. Re-encrypt all existing credentials with the new key
3. Update the key reference in the service

**Note:** Key rotation is not yet implemented. For production, implement a key rotation strategy.

## Troubleshooting

### Decryption Failures

If you see "Credential decryption failed" errors:

1. Check master key configuration:
   ```bash
   echo $ENCRYPTION_MASTER_KEY
   ```

2. Verify encryption_keys table has entries:
   ```sql
   SELECT * FROM encryption_keys;
   ```

3. Check application logs for encryption errors

### Migration from Unencrypted Credentials

If you have existing storage nodes with plain-text credentials:

1. Back up the database
2. Create a migration script to encrypt existing credentials
3. Use `CredentialEncryptionService` to encrypt each credential
4. Update the database records

Example migration (pseudo-code):
```java
// Fetch all nodes with plain credentials
List<StorageNodeEntity> nodes = repository.findAll();

for (StorageNodeEntity node : nodes) {
    if (!isEncrypted(node.getAccessKey())) {
        String encrypted = credentialEncryptionService.encryptCredential(node.getAccessKey());
        node.setAccessKey(encrypted);
    }
    if (!isEncrypted(node.getSecretKey())) {
        String encrypted = credentialEncryptionService.encryptCredential(node.getSecretKey());
        node.setSecretKey(encrypted);
    }
}
repository.saveAll(nodes);
```

## Testing

Test credential encryption:

```bash
# 1. Register a node with credentials
curl -X POST http://localhost:8080/api/v1/storage/nodes \
  -H "Content-Type: application/json" \
  -d '{"nodeId":"test-node","storageType":"MINIO","nodeUrl":"http://localhost:9000","accessKey":"testkey","secretKey":"testsecret","totalCapacityGb":100,"status":"ACTIVE","healthCheckUrl":"http://localhost:9000/health"}'

# 2. Verify credentials are encrypted in database
psql -U postgres -d postgres -c "SELECT access_key, secret_key FROM storage_nodes WHERE node_id='test-node';"

# 3. Retrieve the node via API (credentials should work)
curl http://localhost:8080/api/v1/storage/nodes/test-node

# 4. Upload a file to verify storage adapter can use decrypted credentials
curl -X POST http://localhost:8080/api/v1/files/upload \
  -F "file=@test.txt" \
  -F "userId=user123"
```

## Future Enhancements

1. **AWS KMS Integration**
   - Use AWS KMS for key management
   - Enable automatic key rotation
   - Leverage AWS CloudHSM for additional security

2. **Key Rotation**
   - Implement automatic key rotation
   - Support multiple active keys during rotation
   - Maintain key version history

3. **Audit Logging**
   - Log all credential access
   - Track encryption/decryption operations
   - Alert on suspicious patterns

4. **Field-Level Encryption**
   - Extend encryption to other sensitive fields
   - Support different encryption keys per tenant
   - Implement data classification policies
