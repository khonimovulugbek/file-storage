-- Production file storage tables with encrypted metadata

-- File metadata table with encrypted storage paths
CREATE TABLE IF NOT EXISTS file_metadata (
    id                  UUID PRIMARY KEY,
    file_name           VARCHAR(255) NOT NULL,
    content_type        VARCHAR(100) NOT NULL,
    file_size           BIGINT NOT NULL,
    
    -- Checksum for deduplication and integrity
    checksum_algorithm  VARCHAR(20) NOT NULL,
    checksum_hash       VARCHAR(128) NOT NULL,
    
    -- Storage reference (ENCRYPTED - critical security feature)
    storage_type        VARCHAR(20) NOT NULL,  -- MINIO, S3, SFTP
    storage_node_id     VARCHAR(50) NOT NULL,
    encrypted_path      TEXT NOT NULL,         -- AES-256 encrypted absolute path
    encryption_key_ref  VARCHAR(100) NOT NULL, -- Reference to key vault
    bucket_name         VARCHAR(100),
    region              VARCHAR(50),
    
    -- Ownership and metadata
    owner_id            UUID NOT NULL,
    uploaded_at         TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- Constraints
    CONSTRAINT uk_checksum UNIQUE (checksum_hash)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_owner ON file_metadata(owner_id);
CREATE INDEX IF NOT EXISTS idx_storage_node ON file_metadata(storage_node_id);
CREATE INDEX IF NOT EXISTS idx_uploaded_at ON file_metadata(uploaded_at);
CREATE INDEX IF NOT EXISTS idx_status ON file_metadata(status);

-- Storage nodes registry for horizontal scaling
CREATE TABLE IF NOT EXISTS storage_nodes (
    node_id             VARCHAR(50) PRIMARY KEY,
    storage_type        VARCHAR(20) NOT NULL,
    node_url            VARCHAR(500) NOT NULL,
    access_key          VARCHAR(255),          -- Encrypted
    secret_key          TEXT,                  -- Encrypted
    
    -- Capacity tracking
    total_capacity_gb   BIGINT,
    used_capacity_gb    BIGINT DEFAULT 0,
    file_count          BIGINT DEFAULT 0,
    
    -- Status
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    health_check_url    VARCHAR(500),
    last_health_check   TIMESTAMP,
    
    -- Metadata
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL
);

-- Index for finding available nodes
CREATE INDEX IF NOT EXISTS idx_storage_type_status ON storage_nodes(storage_type, status);

-- Encryption keys reference table
CREATE TABLE IF NOT EXISTS encryption_keys (
    key_ref             VARCHAR(100) PRIMARY KEY,
    key_vault_id        VARCHAR(255) NOT NULL,  -- External vault reference (AWS KMS, etc.)
    algorithm           VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    rotated_at          TIMESTAMP,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- Comments for documentation
COMMENT ON TABLE file_metadata IS 'Stores file metadata with encrypted storage paths for security';
COMMENT ON COLUMN file_metadata.encrypted_path IS 'AES-256 encrypted absolute path - NEVER store plaintext paths';
COMMENT ON COLUMN file_metadata.encryption_key_ref IS 'Reference to encryption key in external vault';
COMMENT ON TABLE storage_nodes IS 'Registry of available storage servers for horizontal scaling';
COMMENT ON TABLE encryption_keys IS 'References to encryption keys stored in external vault (AWS KMS, HashiCorp Vault)';
