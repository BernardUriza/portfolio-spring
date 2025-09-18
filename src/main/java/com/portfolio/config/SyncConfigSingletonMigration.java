package com.portfolio.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creado por Bernard Orozco
 */
@Component
public class SyncConfigSingletonMigration {

    private static final Logger log = LoggerFactory.getLogger(SyncConfigSingletonMigration.class);

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    @Transactional
    public void migrate() {
        // Run best-effort DDL/DML to enforce singleton semantics without Flyway
        tryStep("Add column singleton_key",
                "ALTER TABLE sync_config ADD COLUMN singleton_key CHAR(1)");

        tryStep("Backfill singleton_key",
                "UPDATE sync_config SET singleton_key='X' WHERE singleton_key IS NULL");

        tryStep("Delete duplicates keeping MIN(id)",
                "DELETE FROM sync_config WHERE id NOT IN (SELECT MIN(id) FROM sync_config)");

        tryStep("Set singleton_key NOT NULL",
                "ALTER TABLE sync_config ALTER COLUMN singleton_key SET NOT NULL");

        tryStep("Add unique constraint on singleton_key",
                "ALTER TABLE sync_config ADD CONSTRAINT uk_sync_config_singleton UNIQUE (singleton_key)");
    }

    private void tryStep(String label, String sql) {
        try {
            em.createNativeQuery(sql).executeUpdate();
            log.info("[SyncConfigMigration] {}: OK", label);
        } catch (Exception e) {
            // Ignore if already applied or not applicable
            log.debug("[SyncConfigMigration] {}: skipped ({})", label, e.getMessage());
        }
    }
}

