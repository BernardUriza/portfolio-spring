package com.portfolio.config;

import com.portfolio.service.SyncConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Initializes sync_config table on application startup
 * Creado por Bernard Orozco
 */
@Configuration
@Order(1) // Run early in startup
public class SyncConfigInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SyncConfigInitializer.class);

    private final SyncConfigService syncConfigService;

    public SyncConfigInitializer(SyncConfigService syncConfigService) {
        this.syncConfigService = syncConfigService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("Initializing sync configuration...");
            // This will create default config if table is empty
            var config = syncConfigService.getOrCreate();
            log.info("Sync configuration initialized: enabled={}, intervalHours={}",
                    config.getEnabled(), config.getIntervalHours());
        } catch (Exception e) {
            log.error("Failed to initialize sync configuration: {}", e.getMessage(), e);
            // Don't fail startup, just log the error
        }
    }
}