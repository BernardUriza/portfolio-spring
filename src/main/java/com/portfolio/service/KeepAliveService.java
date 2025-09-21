package com.portfolio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service to keep the Render server alive by sending periodic health checks.
 * This prevents the free tier server from going to sleep after 15 minutes of inactivity.
 * Created by Bernard Orozco
 */
@Service
@ConditionalOnProperty(name = "app.keepalive.enabled", havingValue = "true", matchIfMissing = false)
public class KeepAliveService {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveService.class);

    @Value("${app.keepalive.url:}")
    private String keepAliveUrl;

    @Value("${app.keepalive.interval:840000}") // Default: 14 minutes (just under Render's 15-minute sleep timeout)
    private long keepAliveInterval;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastSuccessTime = new AtomicLong(0);

    @PostConstruct
    public void init() {
        if (keepAliveUrl == null || keepAliveUrl.isEmpty()) {
            log.warn("Keep-alive service is enabled but no URL is configured. Service will be inactive.");
        } else {
            log.info("Keep-alive service initialized. Will ping {} every {} ms", keepAliveUrl, keepAliveInterval);
        }
    }

    /**
     * Scheduled method to ping the health endpoint and keep the server alive.
     * Runs at a fixed delay to prevent Render from putting the server to sleep.
     */
    @Scheduled(fixedDelayString = "${app.keepalive.interval:840000}") // 14 minutes
    public void keepAlive() {
        if (keepAliveUrl == null || keepAliveUrl.isEmpty()) {
            return;
        }

        try {
            log.debug("Sending keep-alive ping to {}", keepAliveUrl);
            ResponseEntity<Map> response = restTemplate.getForEntity(keepAliveUrl, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                successCount.incrementAndGet();
                lastSuccessTime.set(System.currentTimeMillis());
                log.debug("Keep-alive ping successful. Total successful pings: {}", successCount.get());
            } else {
                failureCount.incrementAndGet();
                log.warn("Keep-alive ping returned non-OK status: {}. Total failures: {}",
                        response.getStatusCode(), failureCount.get());
            }
        } catch (Exception e) {
            failureCount.incrementAndGet();
            log.error("Keep-alive ping failed. Total failures: {}. Error: {}",
                     failureCount.get(), e.getMessage());
        }
    }

    /**
     * Get current keep-alive statistics
     */
    public KeepAliveStats getStats() {
        return new KeepAliveStats(
            successCount.get(),
            failureCount.get(),
            lastSuccessTime.get(),
            keepAliveUrl,
            keepAliveInterval
        );
    }

    /**
     * Statistics class for keep-alive service
     */
    public static class KeepAliveStats {
        public final int successCount;
        public final int failureCount;
        public final long lastSuccessTime;
        public final String url;
        public final long intervalMs;

        public KeepAliveStats(int successCount, int failureCount, long lastSuccessTime,
                             String url, long intervalMs) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.lastSuccessTime = lastSuccessTime;
            this.url = url;
            this.intervalMs = intervalMs;
        }
    }
}