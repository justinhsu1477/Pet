package com.pet.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康檢查回應 DTO
 * 用於 Kubernetes liveness/readiness probe 或監控系統
 */
public record HealthCheckDto(
        String status,
        LocalDateTime timestamp,
        String version,
        Map<String, ComponentHealth> components
) {
    public record ComponentHealth(
            String status,
            String message
    ) {
        public static ComponentHealth up() {
            return new ComponentHealth("UP", null);
        }

        public static ComponentHealth up(String message) {
            return new ComponentHealth("UP", message);
        }

        public static ComponentHealth down(String message) {
            return new ComponentHealth("DOWN", message);
        }
    }
}
