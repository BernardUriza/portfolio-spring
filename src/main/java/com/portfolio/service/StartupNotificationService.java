package com.portfolio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Service to log startup information and notify when the server is ready.
 * Helps diagnose Render wake-up issues and monitor server health.
 * Created by Bernard Orozco
 */
@Service
public class StartupNotificationService {

    private static final Logger log = LoggerFactory.getLogger(StartupNotificationService.class);

    @Autowired
    private Environment environment;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${spring.application.name:portfolio-backend}")
    private String applicationName;

    private final long startTime = System.currentTimeMillis();

    /**
     * Logs comprehensive startup information when the application is ready.
     * This helps track when Render instances wake up from sleep.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        long startupTime = System.currentTimeMillis() - startTime;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            String[] activeProfiles = environment.getActiveProfiles();
            String profilesStr = activeProfiles.length > 0 ?
                String.join(", ", activeProfiles) : "default";

            log.info("========================================");
            log.info("🚀 {} is ready!", applicationName);
            log.info("========================================");
            log.info("Timestamp: {}", timestamp);
            log.info("Startup time: {} ms", startupTime);
            log.info("Active profiles: [{}]", profilesStr);
            log.info("Server port: {}", serverPort);
            log.info("Host: {} ({})", hostName, hostAddress);

            // Log Render-specific environment variables if present
            if (environment.getProperty("RENDER") != null) {
                log.info("Running on Render platform");
                log.info("Service: {}", environment.getProperty("RENDER_SERVICE_NAME", "unknown"));
                log.info("Instance: {}", environment.getProperty("RENDER_INSTANCE_ID", "unknown"));
                log.info("Region: {}", environment.getProperty("RENDER_REGION", "unknown"));
            }

            // Check if this is a wake-up from sleep (startup time > 5 seconds usually indicates cold start)
            if (startupTime > 5000) {
                log.warn("⚠️ Slow startup detected ({} ms) - possible wake from sleep", startupTime);
            }

            log.info("========================================");

            // Log available endpoints for debugging
            log.info("Available health endpoints:");
            log.info("  - /api/health (main health check)");
            log.info("  - /actuator/health (Spring Boot actuator)");
            log.info("  - /actuator/health/liveness (Kubernetes liveness)");
            log.info("  - /actuator/health/readiness (Kubernetes readiness)");

        } catch (Exception e) {
            log.error("Error logging startup information: {}", e.getMessage());
        }
    }

    /**
     * Get current server status information
     */
    public ServerStatus getServerStatus() {
        long uptime = System.currentTimeMillis() - startTime;
        String[] activeProfiles = environment.getActiveProfiles();

        return new ServerStatus(
            applicationName,
            activeProfiles.length > 0 ? Arrays.asList(activeProfiles) : Arrays.asList("default"),
            uptime,
            serverPort,
            environment.getProperty("RENDER") != null
        );
    }

    /**
     * Server status information class
     */
    public static class ServerStatus {
        public final String applicationName;
        public final java.util.List<String> activeProfiles;
        public final long uptimeMs;
        public final int port;
        public final boolean isRenderEnvironment;

        public ServerStatus(String applicationName, java.util.List<String> activeProfiles,
                          long uptimeMs, int port, boolean isRenderEnvironment) {
            this.applicationName = applicationName;
            this.activeProfiles = activeProfiles;
            this.uptimeMs = uptimeMs;
            this.port = port;
            this.isRenderEnvironment = isRenderEnvironment;
        }
    }
}