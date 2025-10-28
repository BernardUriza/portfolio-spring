package com.portfolio.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Validates database credentials at application startup
 * Prevents silent failures when credentials are missing or empty
 *
 * Created as part of SEC-001: Remove Hardcoded Database Credentials
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 */
@Configuration
public class DatasourceStartupValidator {

    private static final Logger log = LoggerFactory.getLogger(DatasourceStartupValidator.class);

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @PostConstruct
    public void validateDatasourceCredentials() {
        log.info("🔍 Validating database credentials at startup...");

        boolean hasErrors = false;
        StringBuilder errorMessage = new StringBuilder("Database credential validation failed:\n");

        // Validate URL
        if (datasourceUrl == null || datasourceUrl.trim().isEmpty()) {
            hasErrors = true;
            errorMessage.append("  ❌ SPRING_DATASOURCE_URL is not set or empty\n");
            log.error("❌ SPRING_DATASOURCE_URL environment variable is missing");
        } else {
            log.info("✅ Database URL configured: {}", maskUrl(datasourceUrl));
        }

        // Validate Username
        if (datasourceUsername == null || datasourceUsername.trim().isEmpty()) {
            hasErrors = true;
            errorMessage.append("  ❌ SPRING_DATASOURCE_USERNAME is not set or empty\n");
            log.error("❌ SPRING_DATASOURCE_USERNAME environment variable is missing");
        } else {
            log.info("✅ Database username configured: {}", datasourceUsername);
        }

        // Validate Password
        if (datasourcePassword == null || datasourcePassword.trim().isEmpty()) {
            hasErrors = true;
            errorMessage.append("  ❌ SPRING_DATASOURCE_PASSWORD is not set or empty\n");
            log.error("❌ SPRING_DATASOURCE_PASSWORD environment variable is missing");
        } else {
            log.info("✅ Database password configured (length: {} chars)", datasourcePassword.length());
        }

        if (hasErrors) {
            errorMessage.append("\n");
            errorMessage.append("🔧 SOLUTION:\n");
            errorMessage.append("  1. For local development: Create .env file with credentials\n");
            errorMessage.append("     cp .env.example .env\n");
            errorMessage.append("     Edit .env and set SPRING_DATASOURCE_URL, USERNAME, PASSWORD\n");
            errorMessage.append("\n");
            errorMessage.append("  2. For production: Set environment variables in Render/Neon\n");
            errorMessage.append("     - Render uses DATABASE_URL (auto-parsed by render-entrypoint.sh)\n");
            errorMessage.append("     - Alternative: Set SPRING_DATASOURCE_* variables manually\n");
            errorMessage.append("\n");
            errorMessage.append("  3. See SECURITY_CREDENTIALS.md for complete setup instructions\n");

            log.error(errorMessage.toString());
            throw new IllegalStateException(errorMessage.toString());
        }

        log.info("✅ Database credentials validated successfully - application startup can continue");
    }

    /**
     * Masks sensitive URL parts for logging
     */
    private String maskUrl(String url) {
        if (url == null) {
            return "null";
        }

        // Mask password in connection string if present
        // Example: jdbc:postgresql://user:PASSWORD@host:5432/db -> jdbc:postgresql://user:***@host:5432/db
        if (url.contains("@")) {
            int atIndex = url.indexOf("@");
            int colonBeforeAt = url.lastIndexOf(":", atIndex);
            if (colonBeforeAt > 0) {
                String beforePassword = url.substring(0, colonBeforeAt + 1);
                String afterPassword = url.substring(atIndex);
                return beforePassword + "***" + afterPassword;
            }
        }

        // Just show protocol and host for JDBC URLs
        if (url.startsWith("jdbc:")) {
            int thirdSlash = url.indexOf("/", url.indexOf("//") + 2);
            if (thirdSlash > 0) {
                return url.substring(0, thirdSlash) + "/***";
            }
        }

        return url;
    }
}
