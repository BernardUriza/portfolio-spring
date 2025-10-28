package com.portfolio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Service for managing application alerts and notifications
 * Tracks critical system events and provides notification mechanisms
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private static final int MAX_ALERT_HISTORY = 100;

    private final ConcurrentLinkedQueue<Alert> alertHistory = new ConcurrentLinkedQueue<>();

    /**
     * Send a critical alert
     * Currently logs to console but can be extended for email/webhook notifications
     */
    public void sendCriticalAlert(String title, String message) {
        Alert alert = new Alert(
                AlertSeverity.CRITICAL,
                title,
                message,
                LocalDateTime.now()
        );

        alertHistory.offer(alert);
        trimAlertHistory();

        log.error("🚨 CRITICAL ALERT: {} - {}", title, message);

        // TODO: Add email notification integration
        // TODO: Add webhook notification integration
        // TODO: Add admin dashboard notification
    }

    /**
     * Send a warning alert
     */
    public void sendWarningAlert(String title, String message) {
        Alert alert = new Alert(
                AlertSeverity.WARNING,
                title,
                message,
                LocalDateTime.now()
        );

        alertHistory.offer(alert);
        trimAlertHistory();

        log.warn("⚠️  WARNING ALERT: {} - {}", title, message);
    }

    /**
     * Send an informational alert
     */
    public void sendInfoAlert(String title, String message) {
        Alert alert = new Alert(
                AlertSeverity.INFO,
                title,
                message,
                LocalDateTime.now()
        );

        alertHistory.offer(alert);
        trimAlertHistory();

        log.info("ℹ️  INFO ALERT: {} - {}", title, message);
    }

    /**
     * Get recent alerts
     */
    public List<Alert> getRecentAlerts(int limit) {
        return alertHistory.stream()
                .limit(Math.min(limit, MAX_ALERT_HISTORY))
                .toList();
    }

    /**
     * Get all alerts
     */
    public List<Alert> getAllAlerts() {
        return new ArrayList<>(alertHistory);
    }

    /**
     * Clear alert history
     */
    public void clearAlerts() {
        alertHistory.clear();
        log.info("Alert history cleared");
    }

    /**
     * Trim alert history to max size
     */
    private void trimAlertHistory() {
        while (alertHistory.size() > MAX_ALERT_HISTORY) {
            alertHistory.poll();
        }
    }

    /**
     * Alert severity levels
     */
    public enum AlertSeverity {
        CRITICAL,
        WARNING,
        INFO
    }

    /**
     * Alert record
     */
    public record Alert(
            AlertSeverity severity,
            String title,
            String message,
            LocalDateTime timestamp
    ) {
        public String getSeverityLabel() {
            return switch (severity) {
                case CRITICAL -> "🚨 CRITICAL";
                case WARNING -> "⚠️  WARNING";
                case INFO -> "ℹ️  INFO";
            };
        }
    }
}
