package com.file_storage.infrastructure.adapter.storage;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import com.file_storage.application.port.out.storage.FileStoragePort;
import com.file_storage.application.port.out.storage.StorageContext;
import com.file_storage.application.port.out.storage.StorageResult;
import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.domain.model.storage.StorageReference;
import com.file_storage.infrastructure.annotation.Adapter;

import java.io.InputStream;

@Adapter
@Slf4j
public class SFTPStorageAdapter implements FileStoragePort {
    private final JSch jsch;

    public SFTPStorageAdapter(JSch jsch) {
        this.jsch = jsch;
    }

    @Override
    public StorageResult store(InputStream content, StorageContext context) {
        Session session = null;
        ChannelSftp sftpChannel = null;
        try {
            StorageNode storageNode = context.storageNode();
            session = createSession(storageNode);
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            String remotePath = buildRemotePath(context);
            String directoryPath = getDirectoryPath("/" + storageNode.bucket() + remotePath);
            ensureDirectoryExists(sftpChannel, directoryPath);

            sftpChannel.put(content, "/" + storageNode.bucket() + remotePath);
            log.info("File uploaded to SFTP: {}", remotePath);

            return StorageResult.builder()
                    .absolutePath(storageNode.publicNodeUrl() + remotePath)
                    .path(remotePath)
                    .bucket(null)
                    .etag(null)
                    .bytes(context.fileSize())
                    .region(null)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeResources(sftpChannel, session);
        }
    }

    @Override
    public InputStream retrieve(StorageReference storageReference) {
        return null;
    }

    private void ensureDirectoryExists(ChannelSftp channel, String directory) throws SftpException {
        String[] dirs = directory.split("/");
        StringBuilder currentPath = new StringBuilder();
        for (String dir : dirs) {
            if (dir.isBlank()) continue;
            currentPath.append("/").append(dir);
            try {
                channel.stat(currentPath.toString());
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    channel.mkdir(currentPath.toString());
                }
            }
        }
    }

    private String getDirectoryPath(String fullPath) {
        int lastSlash = fullPath.lastIndexOf('/');
        return lastSlash > 0 ? fullPath.substring(0, lastSlash) : "/";
    }

    private String buildRemotePath(StorageContext context) {
        String basePath = context.basePath() == null ? "" : context.basePath();
        return basePath.isBlank()
                ? "/" + context.fileName()
                : "/" + basePath + "/" + context.fileName();
    }

    private Session createSession(StorageNode storageNode) throws JSchException {
        String accessKey = storageNode.accessKey();
        String secretKey = storageNode.secretKey();
        String nodeUrl = storageNode.nodeUrl();

        String[] parts = nodeUrl.split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 22;

        Session session = jsch.getSession(accessKey, host, port);
        session.setPassword(secretKey);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
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
