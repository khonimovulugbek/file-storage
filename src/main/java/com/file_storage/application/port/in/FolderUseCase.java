package com.file_storage.application.port.in;

import com.file_storage.domain.model.Folder;

import java.util.List;
import java.util.UUID;

public interface FolderUseCase {
    Folder createFolder(String name, UUID parentFolderId, UUID userId);
    Folder getFolderById(UUID folderId, UUID userId);
    List<Folder> listUserFolders(UUID userId);
    List<Folder> listSubFolders(UUID parentFolderId, UUID userId);
    void deleteFolder(UUID folderId, UUID userId);
    Folder updateFolder(UUID folderId, String newName, UUID userId);
}
