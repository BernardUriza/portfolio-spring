package com.portfolio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoints for monitoring and load balancers
 * Created by Bernard Orozco
 *
 * Production-ready health checks following Kubernetes best practices:
 * - /api/health - Liveness probe (is the application running?)
 * - /api/ready - Readiness probe (can the application serve traffic?)
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * Liveness probe - Simple health check that doesn't require authentication
     * Used by load balancers and monitoring systems
     *
     * Returns 200 OK if application is running
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "portfolio-backend");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * Readiness probe - Checks if application can serve traffic
     * Verifies critical dependencies (database, etc.)
     *
     * Returns:
     * - 200 OK if ready to serve traffic
     * - 503 Service Unavailable if not ready (e.g., database unreachable)
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> checks = new HashMap<>();

        boolean isReady = true;

        // Check database connectivity
        try {
            if (dataSource != null) {
                try (Connection conn = dataSource.getConnection()) {
                    checks.put("database", conn.isValid(3) ? "UP" : "DOWN");
                    if (!conn.isValid(3)) {
                        isReady = false;
                    }
                }
            } else {
                checks.put("database", "NOT_CONFIGURED");
            }
        } catch (Exception e) {
            checks.put("database", "DOWN");
            checks.put("database_error", e.getMessage());
            isReady = false;
        }

        response.put("status", isReady ? "READY" : "NOT_READY");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "portfolio-backend");
        response.put("checks", checks);

        return ResponseEntity
                .status(isReady ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * Root API endpoint for basic connectivity check
     * Returns API metadata
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Portfolio API");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("endpoints", Map.of(
                "health", "/api/health",
                "ready", "/api/ready",
                "admin", "/api/admin/*"
        ));
        return ResponseEntity.ok(response);
    }
}