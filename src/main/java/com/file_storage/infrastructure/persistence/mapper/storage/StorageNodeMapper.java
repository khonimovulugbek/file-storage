package com.file_storage.infrastructure.persistence.mapper.storage;

import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.infrastructure.annotation.Mapper;
import com.file_storage.infrastructure.properties.StorageNodesConfig;

import java.util.Collections;
import java.util.List;

@Mapper
public class StorageNodeMapper {

    public StorageNode toDomain(StorageNodesConfig.NodeConfig entity) {
        if (entity == null) return null;
        return StorageNode.builder()
                .nodeId(entity.id())
                .storageType(entity.type())
                .nodeUrl(entity.endpoint())
                .publicNodeUrl(entity.publicEndpoint())
                .accessKey(entity.accessKey())
                .secretKey(entity.secretKey())
                .bucket(entity.bucket())
                .status(entity.status())
                .build();
    }

    public List<StorageNode> toDomain(List<StorageNodesConfig.NodeConfig> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(this::toDomain).toList();
    }
}
