package com.file_storage.infrastructure.web.controller.storage;

import com.file_storage.application.port.out.storage.StorageNodeRegistryPort;
import com.file_storage.domain.model.storage.StorageNode;
import com.file_storage.infrastructure.web.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/storage/nodes")
@RequiredArgsConstructor
@Slf4j
public class StorageNodeController {

    private final StorageNodeRegistryPort storageNodeRegistry;


    @PostMapping
    public ResponseEntity<ApiResponse<StorageNodeResponse>> registerNode(
            @RequestBody StorageNodeRequest request) {

        try {
            log.info("Registering new storage node: {} (type: {})", request.nodeId(), request.storageType());

            StorageNode node = StorageNode.builder()
                    .nodeId(request.nodeId())
                    .storageType(com.file_storage.domain.model.storage.StorageType.valueOf(request.storageType()))
                    .nodeUrl(request.nodeUrl())
                    .totalCapacityGb(request.totalCapacityGb())
                    .usedCapacityGb(0L)
                    .fileCount(0L)
                    .accessKey(request.accessKey())
                    .secretKey(request.secretKey())
                    .status(StorageNode.NodeStatus.valueOf(request.status()))
                    .healthCheckUrl(request.healthCheckUrl())
                    .lastHealthCheck(null)
                    .build();

            StorageNode registered = storageNodeRegistry.registerNode(node);

            StorageNodeResponse response = mapToResponse(registered);

            log.info("Storage node registered successfully: {}", registered.nodeId());

            return ResponseEntity.ok(ApiResponse.success("Storage node registered successfully", response));

        } catch (Exception e) {
            log.error("Failed to register storage node", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to register storage node: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StorageNodeResponse>>> getAllNodes() {
        try {
            List<StorageNode> nodes = storageNodeRegistry.findAll();

            List<StorageNodeResponse> response = nodes.stream()
                    .map(this::mapToResponse)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success("Storage nodes retrieved successfully", response));

        } catch (Exception e) {
            log.error("Failed to retrieve storage nodes", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve storage nodes: " + e.getMessage()));
        }
    }

    @GetMapping("/{nodeId}")
    public ResponseEntity<ApiResponse<StorageNodeResponse>> getNode(@PathVariable String nodeId) {
        try {
            StorageNode node = storageNodeRegistry.findById(nodeId)
                    .orElseThrow(() -> new RuntimeException("Storage node not found: " + nodeId));

            StorageNodeResponse response = mapToResponse(node);

            return ResponseEntity.ok(ApiResponse.success("Storage node retrieved successfully", response));

        } catch (Exception e) {
            log.error("Failed to retrieve storage node: {}", nodeId, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{nodeId}/status")
    public ResponseEntity<ApiResponse<Void>> updateNodeStatus(
            @PathVariable String nodeId,
            @RequestParam String status) {

        try {
            StorageNode.NodeStatus nodeStatus = StorageNode.NodeStatus.valueOf(status.toUpperCase());
            storageNodeRegistry.updateNodeStatus(nodeId, nodeStatus);

            log.info("Updated node {} status to {}", nodeId, nodeStatus);

            return ResponseEntity.ok(ApiResponse.success("Node status updated successfully", null));

        } catch (Exception e) {
            log.error("Failed to update node status: {}", nodeId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update node status: " + e.getMessage()));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<StorageNodeResponse>>> getAvailableNodes(
            @RequestParam(required = false) String storageType) {

        try {
            com.file_storage.domain.model.storage.StorageType type = storageType != null
                    ? com.file_storage.domain.model.storage.StorageType.valueOf(storageType.toUpperCase())
                    : null;

            List<StorageNode> nodes = storageNodeRegistry.findAvailableNodes(type);

            List<StorageNodeResponse> response = nodes.stream()
                    .map(this::mapToResponse)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success("Available nodes retrieved successfully", response));

        } catch (Exception e) {
            log.error("Failed to retrieve available nodes", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve available nodes: " + e.getMessage()));
        }
    }

    private StorageNodeResponse mapToResponse(StorageNode node) {
        return new StorageNodeResponse(
                node.nodeId(),
                node.storageType().name(),
                node.nodeUrl(),
                node.totalCapacityGb(),
                node.usedCapacityGb(),
                node.fileCount(),
                node.status().name(),
                node.getUsedCapacityPercent(),
                node.getAvailableCapacityGb(),
                node.isAvailable(),
                node.healthCheckUrl(),
                node.lastHealthCheck()
        );
    }

    public record StorageNodeRequest(
            String nodeId,
            String storageType,
            String nodeUrl,
            String accessKey,
            String secretKey,
            Long totalCapacityGb,
            String status,
            String healthCheckUrl
    ) {
    }

    public record StorageNodeResponse(
            String nodeId,
            String storageType,
            String nodeUrl,
            Long totalCapacityGb,
            Long usedCapacityGb,
            Long fileCount,
            String status,
            Double usedCapacityPercent,
            Long availableCapacityGb,
            Boolean isAvailable,
            String healthCheckUrl,
            LocalDateTime lastHealthCheck
    ) {
    }
}
