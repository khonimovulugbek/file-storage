package com.file_storage.infrastructure.health;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
public class HealthCheckController {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());

        Map<String, String> components = new HashMap<>();
        components.put("database", checkDatabase());
        components.put("redis", checkRedis());

        health.put("components", components);

        boolean allHealthy = components.values().stream().allMatch(s -> s.equals("UP"));
        health.put("status", allHealthy ? "UP" : "DOWN");

        return ResponseEntity.ok(health);
    }

    @GetMapping("/health/liveness")
    public ResponseEntity<Map<String, String>> liveness() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/readiness")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        
        boolean dbReady = checkDatabase().equals("UP");
        boolean redisReady = checkRedis().equals("UP");
        
        response.put("database", dbReady);
        response.put("redis", redisReady);
        response.put("ready", dbReady && redisReady);

        return ResponseEntity.ok(response);
    }

    private String checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2) ? "UP" : "DOWN";
        } catch (Exception e) {
            return "DOWN";
        }
    }

    private String checkRedis() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return "UP";
        } catch (Exception e) {
            return "DOWN";
        }
    }
}
