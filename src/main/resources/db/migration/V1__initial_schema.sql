-- Production-ready database schema for multi-instance file storage system
-- Optimized for horizontal scalability

-- File Metadata Table
CREATE TABLE IF NOT EXISTS file_metadata (
    id UUID PRIMARY KEY,
    
    -- File metadata
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    
    -- Checksum for deduplication and integrity
    checksum_algorithm VARCHAR(20) NOT NULL,
    checksum_hash VARCHAR(128) NOT NULL,
    
    -- Encrypted storage reference (CRITICAL: Never store plaintext paths)
    storage_type VARCHAR(20) NOT NULL,
    storage_node_id VARCHAR(50) NOT NULL,
    encrypted_path TEXT NOT NULL,
    encryption_key_ref VARCHAR(100) NOT NULL,
    bucket_name VARCHAR(100),
    region VARCHAR(50),
    
    -- Ownership and metadata
    owner_id UUID NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- Constraints
    CONSTRAINT uk_checksum_hash UNIQUE (checksum_hash)
);

-- Indexes for performance
CREATE INDEX idx_file_metadata_owner ON file_metadata(owner_id);
CREATE INDEX idx_file_metadata_storage_node ON file_metadata(storage_node_id);
CREATE INDEX idx_file_metadata_uploaded_at ON file_metadata(uploaded_at DESC);
CREATE INDEX idx_file_metadata_status ON file_metadata(status);

-- Storage Nodes Registry
CREATE TABLE IF NOT EXISTS storage_nodes (
    node_id VARCHAR(50) PRIMARY KEY,
    
    -- Node configuration
    storage_type VARCHAR(20) NOT NULL,
    node_url VARCHAR(500) NOT NULL,
    access_key VARCHAR(255),
    secret_key TEXT,
    
    -- Capacity tracking (for load balancing)
    total_capacity_gb BIGINT,
    used_capacity_gb BIGINT DEFAULT 0,
    file_count BIGINT DEFAULT 0,
    
    -- Health and status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    health_check_url VARCHAR(500),
    last_health_check TIMESTAMP,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Indexes for storage node queries
CREATE INDEX idx_storage_nodes_type_status ON storage_nodes(storage_type, status);

-- Encryption Keys Table (for multi-instance key sharing)
CREATE TABLE IF NOT EXISTS encryption_keys (
    key_ref VARCHAR(100) PRIMARY KEY,
    
    -- Encrypted key (encrypted with master key)
    encrypted_key TEXT NOT NULL,
    
    -- Key metadata
    algorithm VARCHAR(50) NOT NULL,
    vault_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- Index for active keys
CREATE INDEX idx_encryption_keys_status ON encryption_keys(status);

-- Comments for documentation
COMMENT ON TABLE file_metadata IS 'Stores file metadata with encrypted storage paths for security';
COMMENT ON COLUMN file_metadata.encrypted_path IS 'AES-256-GCM encrypted storage path - NEVER store plaintext';
COMMENT ON COLUMN file_metadata.checksum_hash IS 'SHA-256 hash for deduplication and integrity verification';
COMMENT ON TABLE storage_nodes IS 'Registry of available storage backends for horizontal scaling';
COMMENT ON TABLE encryption_keys IS 'Encryption keys encrypted with master key for multi-instance access';
