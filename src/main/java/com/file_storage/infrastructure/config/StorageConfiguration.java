package com.file_storage.infrastructure.config;

import com.file_storage.application.port.out.storage.EncryptionPort;
import com.file_storage.domain.service.LeastUsedNodeSelectionStrategy;
import com.file_storage.domain.service.MetadataEncryptionService;
import com.file_storage.domain.service.NodeSelectionStrategy;
import com.file_storage.domain.service.StorageSelectionService;
import com.jcraft.jsch.JSch;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Configuration for storage backends and domain services
 */
@Configuration
public class StorageConfiguration {
    
    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;
    
    @Value("${minio.access-key:minioadmin}")
    private String minioAccessKey;
    
    @Value("${minio.secret-key:minioadmin}")
    private String minioSecretKey;
    
    @Value("${aws.region:us-east-1}")
    private String awsRegion;
    
    @Value("${aws.access-key:1}")
    private String awsAccessKey;
    
    @Value("${aws.secret-key:1}")
    private String awsSecretKey;
    
    /**
     * MinIO client configuration
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(minioEndpoint)
            .credentials(minioAccessKey, minioSecretKey)
            .build();
    }
    
    /**
     * AWS S3 client configuration
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
            ))
            .build();
    }
    
    /**
     * S3 Presigner for generating presigned URLs
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
            ))
            .build();
    }
    
    /**
     * JSch for SFTP connections
     */
    @Bean
    public JSch jsch() {
        return new JSch();
    }
    
    /**
     * Storage selection service (domain service)
     */
    @Bean
    public StorageSelectionService storageSelectionService() {
        return new StorageSelectionService();
    }
    
    /**
     * Metadata encryption service (domain service)
     */
    @Bean
    public MetadataEncryptionService metadataEncryptionService(EncryptionPort encryptionPort) {
        return new MetadataEncryptionService(encryptionPort);
    }
}
