package com.file_storage.infrastructure.storage.adapter;

import com.file_storage.application.port.out.storage.FileStoragePort;
import com.file_storage.application.port.out.storage.StorageContext;
import com.file_storage.application.port.out.storage.StorageResult;
import com.file_storage.domain.model.storage.StorageReference;
import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * SFTP storage adapter implementing FileStoragePort
 * Handles file operations with SFTP servers
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SFTPStorageAdapter implements FileStoragePort {
    
    private final JSch jsch;
    
    @Override
    public StorageResult store(InputStream content, StorageContext context) {
        Session session = null;
        ChannelSftp sftpChannel = null;
        
        try {
            // Connect to SFTP server
            session = createSession(context.targetNode().nodeUrl());
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            
            // Build remote path
            String remotePath = buildRemotePath(context);
            
            // Ensure directory exists
            ensureDirectoryExists(sftpChannel, getDirectoryPath(remotePath));
            
            // Upload file
            sftpChannel.put(content, remotePath);
            
            log.info("File uploaded to SFTP: {}", remotePath);
            
            return StorageResult.builder()
                .absolutePath(remotePath)
                .bucket(null)  // SFTP doesn't use buckets
                .etag(null)
                .uploadedBytes(context.fileSize())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to upload file to SFTP", e);
            throw new RuntimeException("SFTP upload failed", e);
        } finally {
            closeResources(sftpChannel, session);
        }
    }
    
    @Override
    public InputStream retrieve(StorageReference reference, String decryptedPath) {
        Session session = null;
        ChannelSftp sftpChannel = null;
        
        try {
            session = createSession(getNodeUrl(reference));
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            
            log.info("Retrieving file from SFTP: {}", decryptedPath);
            
            return sftpChannel.get(decryptedPath);
            
        } catch (Exception e) {
            log.error("Failed to retrieve file from SFTP: {}", decryptedPath, e);
            closeResources(sftpChannel, session);
            throw new RuntimeException("SFTP retrieval failed", e);
        }
    }
    
    @Override
    public void delete(StorageReference reference, String decryptedPath) {
        Session session = null;
        ChannelSftp sftpChannel = null;
        
        try {
            session = createSession(getNodeUrl(reference));
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            
            sftpChannel.rm(decryptedPath);
            
            log.info("File deleted from SFTP: {}", decryptedPath);
            
        } catch (Exception e) {
            log.error("Failed to delete file from SFTP: {}", decryptedPath, e);
            throw new RuntimeException("SFTP deletion failed", e);
        } finally {
            closeResources(sftpChannel, session);
        }
    }
    
    @Override
    public boolean exists(StorageReference reference, String decryptedPath) {
        Session session = null;
        ChannelSftp sftpChannel = null;
        
        try {
            session = createSession(getNodeUrl(reference));
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            
            sftpChannel.stat(decryptedPath);
            return true;
            
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
            log.error("Error checking SFTP file existence: {}", decryptedPath, e);
            return false;
        } catch (Exception e) {
            log.error("Error checking SFTP file existence: {}", decryptedPath, e);
            return false;
        } finally {
            closeResources(sftpChannel, session);
        }
    }
    
    @Override
    public String generatePresignedUrl(StorageReference reference, String decryptedPath, int expirationSeconds) {
        // SFTP doesn't support presigned URLs
        log.warn("Presigned URLs not supported for SFTP storage");
        return null;
    }
    
    private Session createSession(String nodeUrl) throws JSchException {
        // Parse nodeUrl: sftp://user@host:port
        String[] parts = nodeUrl.replace("sftp://", "").split("@");
        String user = parts[0];
        String[] hostPort = parts[1].split(":");
        String host = hostPort[0];
        int port = hostPort.length > 1 ? Integer.parseInt(hostPort[1]) : 22;
        
        Session session = jsch.getSession(user, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        
        return session;
    }
    
    private void ensureDirectoryExists(ChannelSftp channel, String directory) throws SftpException {
        String[] dirs = directory.split("/");
        String currentPath = "";
        
        for (String dir : dirs) {
            if (dir.isEmpty()) continue;
            
            currentPath += "/" + dir;
            try {
                channel.stat(currentPath);
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    channel.mkdir(currentPath);
                }
            }
        }
    }
    
    private String buildRemotePath(StorageContext context) {
        String basePath = context.basePath() != null ? context.basePath() : "";
        return basePath.isEmpty() 
            ? "/" + context.fileName()
            : "/" + basePath + "/" + context.fileName();
    }
    
    private String getDirectoryPath(String fullPath) {
        int lastSlash = fullPath.lastIndexOf('/');
        return lastSlash > 0 ? fullPath.substring(0, lastSlash) : "/";
    }
    
    private String getNodeUrl(StorageReference reference) {
        // In production, retrieve from node registry
        // For now, return placeholder
        return "sftp://user@localhost:22";
    }
    
    private void closeResources(ChannelSftp channel, Session session) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
