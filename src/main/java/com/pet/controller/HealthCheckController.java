package com.pet.controller;

import com.pet.dto.HealthCheckDto;
import com.pet.dto.HealthCheckDto.ComponentHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康檢查 Controller
 * 提供系統健康狀態端點，用於：
 * - Kubernetes liveness/readiness probe
 * - 負載均衡器健康檢查
 * - 監控系統 (Prometheus, Grafana 等)
 */
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    private final DataSource dataSource;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    public HealthCheckController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 簡單的存活檢查 (Liveness Probe)
     * 只要應用程式還在運行就回傳 OK
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> liveness() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * 就緒檢查 (Readiness Probe)
     * 檢查應用程式是否準備好接收流量（包含資料庫連線）
     */
    @GetMapping("/ready")
    public ResponseEntity<HealthCheckDto> readiness() {
        Map<String, ComponentHealth> components = new LinkedHashMap<>();
        boolean allHealthy = true;

        // 檢查資料庫連線
        ComponentHealth dbHealth = checkDatabase();
        components.put("database", dbHealth);
        if ("DOWN".equals(dbHealth.status())) {
            allHealthy = false;
        }

        // 檢查記憶體使用量
        ComponentHealth memoryHealth = checkMemory();
        components.put("memory", memoryHealth);

        String overallStatus = allHealthy ? "UP" : "DOWN";

        HealthCheckDto response = new HealthCheckDto(
                overallStatus,
                LocalDateTime.now(),
                appVersion,
                components
        );

        if (allHealthy) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * 完整健康檢查（包含詳細資訊）
     */
    @GetMapping
    public ResponseEntity<HealthCheckDto> fullHealthCheck() {
        return readiness();
    }

    private ComponentHealth checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                return ComponentHealth.up("Database connection is healthy");
            } else {
                return ComponentHealth.down("Database connection validation failed");
            }
        } catch (Exception e) {
            logger.error("Database health check failed", e);
            return ComponentHealth.down("Database error: " + e.getMessage());
        }
    }

    private ComponentHealth checkMemory() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double usagePercentage = (double) usedMemory / maxMemory * 100;

        String message = String.format("Memory usage: %.1f%% (%d MB / %d MB)",
                usagePercentage,
                usedMemory / (1024 * 1024),
                maxMemory / (1024 * 1024));

        // 如果記憶體使用超過 90% 則警告
        if (usagePercentage > 90) {
            return ComponentHealth.down(message);
        }
        return ComponentHealth.up(message);
    }
}
