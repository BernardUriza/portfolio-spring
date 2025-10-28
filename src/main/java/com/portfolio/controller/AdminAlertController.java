package com.portfolio.controller;

import com.portfolio.aspect.RateLimit;
import com.portfolio.aspect.RequiresAdminToken;
import com.portfolio.aspect.RequiresFeature;
import com.portfolio.service.AlertService;
import com.portfolio.service.RateLimitingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin controller for managing system alerts and notifications
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 */
@RestController
@RequestMapping("/api/admin/alerts")
public class AdminAlertController {

    private static final Logger log = LoggerFactory.getLogger(AdminAlertController.class);
    private final AlertService alertService;

    public AdminAlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Get all recent alerts
     */
    @GetMapping
    @RequiresAdminToken
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> getAllAlerts() {
        List<AlertService.Alert> alerts = alertService.getAllAlerts();

        return ResponseEntity.ok(Map.of(
                "alerts", alerts,
                "count", alerts.size(),
                "status", "success"
        ));
    }

    /**
     * Get recent N alerts
     */
    @GetMapping("/recent/{limit}")
    @RequiresAdminToken
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> getRecentAlerts(@PathVariable int limit) {
        if (limit < 1 || limit > 100) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Limit must be between 1 and 100",
                    "status", "error"
            ));
        }

        List<AlertService.Alert> alerts = alertService.getRecentAlerts(limit);

        return ResponseEntity.ok(Map.of(
                "alerts", alerts,
                "count", alerts.size(),
                "limit", limit,
                "status", "success"
        ));
    }

    /**
     * Get alerts by severity
     */
    @GetMapping("/severity/{severity}")
    @RequiresAdminToken
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> getAlertsBySeverity(@PathVariable String severity) {
        try {
            AlertService.AlertSeverity alertSeverity = AlertService.AlertSeverity.valueOf(severity.toUpperCase());
            List<AlertService.Alert> allAlerts = alertService.getAllAlerts();

            List<AlertService.Alert> filteredAlerts = allAlerts.stream()
                    .filter(alert -> alert.severity() == alertSeverity)
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "alerts", filteredAlerts,
                    "count", filteredAlerts.size(),
                    "severity", severity,
                    "status", "success"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid severity. Must be one of: CRITICAL, WARNING, INFO",
                    "status", "error"
            ));
        }
    }

    /**
     * Clear alert history
     */
    @DeleteMapping
    @RequiresAdminToken
    @RequiresFeature("admin_endpoints")
    @RateLimit(type = RateLimitingService.RateLimitType.ADMIN_ENDPOINTS)
    public ResponseEntity<Map<String, Object>> clearAlerts() {
        int countBeforeClear = alertService.getAllAlerts().size();
        alertService.clearAlerts();

        log.info("Admin cleared alert history ({} alerts removed)", countBeforeClear);

        return ResponseEntity.ok(Map.of(
                "message", "Alert history cleared",
                "alertsCleared", countBeforeClear,
                "status", "success"
        ));
    }
}
