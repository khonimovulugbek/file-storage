# Testing Credential Encryption

## Quick Verification Test

Follow these steps to verify credential encryption is working:

### 1. Start the Application

```bash
# Start infrastructure
docker-compose up -d postgres redis minio

# Run the application
./gradlew bootRun
```

### 2. Register a Storage Node with Credentials

```bash
curl -X POST http://localhost:8080/api/v1/storage/nodes \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": "test-minio-1",
    "storageType": "MINIO",
    "nodeUrl": "http://localhost:9000",
    "accessKey": "minioadmin",
    "secretKey": "minioadmin",
    "totalCapacityGb": 100,
    "status": "ACTIVE",
    "healthCheckUrl": "http://localhost:9000/minio/health/live"
  }'
```

Expected response:
```json
{
  "success": true,
  "message": "Storage node registered successfully",
  "data": {
    "nodeId": "test-minio-1",
    "storageType": "MINIO",
    ...
  }
}
```

### 3. Verify Credentials are Encrypted in Database

Connect to PostgreSQL and check the stored credentials:

```bash
docker exec -it file-storage-postgres-1 psql -U postgres -d postgres
```

Run this query:
```sql
SELECT node_id, access_key, secret_key 
FROM storage_nodes 
WHERE node_id = 'test-minio-1';
```

**Expected Result:**
- `access_key` should be encrypted: `AES-256-GCM:dGVzdGl2MTIz:Y2lwaGVydGV4dGhlcmU=`
- `secret_key` should be encrypted: `AES-256-GCM:dGVzdGl2MTIz:Y2lwaGVydGV4dGhlcmU=`
- They should NOT be plain text "minioadmin"

### 4. Verify Credentials are Decrypted When Retrieved

Retrieve the storage node via API:

```bash
curl http://localhost:8080/api/v1/storage/nodes/test-minio-1
```

The response should show the node details (credentials are not exposed in API response for security).

### 5. Verify Storage Adapter Can Use Decrypted Credentials

Upload a test file to verify the storage adapter can connect using decrypted credentials:

```bash
# First, register and login to get a token
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'

curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# Save the token from response
TOKEN="<your-token-here>"

# Upload a test file
echo "Test file content" > test.txt
curl -X POST http://localhost:8080/api/v1/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.txt"
```

**Expected Result:**
- File uploads successfully
- This proves the storage adapter decrypted credentials correctly and connected to MinIO

### 6. Check Application Logs

Look for these log messages:

```bash
# In application logs
grep "Credential encrypted successfully" logs/application.log
grep "Credential decrypted successfully" logs/application.log
```

Or if running with `./gradlew bootRun`, watch the console output.

## Troubleshooting

### Issue: "Encryption key not found"

**Solution:** The encryption key is generated automatically on first use. Check:
```sql
SELECT * FROM encryption_keys;
```

If empty, the key will be created on the first encryption operation.

### Issue: "Decryption failed"

**Possible causes:**
1. Master key changed - credentials encrypted with old key can't be decrypted with new key
2. Database corruption
3. Invalid encrypted data format

**Solution:**
- Verify `encryption.master-key` in application.yaml hasn't changed
- Check encryption_keys table exists and has entries
- Review application logs for detailed error messages

### Issue: "Storage adapter connection failed"

**Possible causes:**
1. Credentials not decrypting correctly
2. MinIO not running
3. Network connectivity issues

**Solution:**
```bash
# Check MinIO is running
docker ps | grep minio

# Test MinIO directly
curl http://localhost:9000/minio/health/live

# Check application logs for decryption errors
```

## Manual Encryption/Decryption Test

You can test the encryption service directly:

```java
// In a test class or main method
@Autowired
private CredentialEncryptionService credentialEncryptionService;

public void testEncryption() {
    String plainText = "my-secret-key";
    
    // Encrypt
    String encrypted = credentialEncryptionService.encryptCredential(plainText);
    System.out.println("Encrypted: " + encrypted);
    
    // Decrypt
    String decrypted = credentialEncryptionService.decryptCredential(encrypted);
    System.out.println("Decrypted: " + decrypted);
    
    // Verify
    assert plainText.equals(decrypted);
    System.out.println("✅ Encryption/Decryption working correctly!");
}
```

## Security Verification Checklist

- [ ] Credentials are encrypted in database (not plain text)
- [ ] Encryption format is `AES-256-GCM:iv:ciphertext`
- [ ] Each credential has unique IV (different encrypted values for same input)
- [ ] Storage adapters can connect using decrypted credentials
- [ ] File upload/download works correctly
- [ ] Master key is not hardcoded in production
- [ ] Encryption keys table is populated
- [ ] Application logs show encryption/decryption operations

## Performance Test

Test encryption performance:

```bash
# Register 100 storage nodes
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/v1/storage/nodes \
    -H "Content-Type: application/json" \
    -d "{
      \"nodeId\": \"node-$i\",
      \"storageType\": \"MINIO\",
      \"nodeUrl\": \"http://localhost:9000\",
      \"accessKey\": \"key-$i\",
      \"secretKey\": \"secret-$i\",
      \"totalCapacityGb\": 100,
      \"status\": \"ACTIVE\",
      \"healthCheckUrl\": \"http://localhost:9000/health\"
    }"
done

# Retrieve all nodes (tests decryption performance)
curl http://localhost:8080/api/v1/storage/nodes
```

Monitor application performance and logs during this test.
