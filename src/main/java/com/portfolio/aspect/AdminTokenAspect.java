package com.portfolio.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

/**
 * Aspect for validating admin token on endpoints marked with @RequiresAdminToken
 *
 * Validates that the X-Admin-Token header matches the configured admin token
 * from portfolio.admin.token property. Returns 403 FORBIDDEN if validation fails.
 *
 * Security features:
 * - Constant-time string comparison to prevent timing attacks
 * - Logs failed attempts with client IP for audit trail
 * - Returns generic error message to prevent information leakage
 * - Validates token is configured (not empty)
 *
 * Created as part of SEC-002: Add Admin Token Validation
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 */
@Aspect
@Component
public class AdminTokenAspect {

    private static final Logger log = LoggerFactory.getLogger(AdminTokenAspect.class);
    private static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";

    @Value("${portfolio.admin.token:}")
    private String configuredAdminToken;

    @Value("${portfolio.admin.security.enabled:true}")
    private boolean securityEnabled;

    /**
     * Intercepts methods annotated with @RequiresAdminToken and validates the token
     */
    @Around("@annotation(requiresAdminToken)")
    public Object validateAdminToken(ProceedingJoinPoint joinPoint, RequiresAdminToken requiresAdminToken) throws Throwable {

        // If security is disabled (e.g., for local development), allow access
        if (!securityEnabled) {
            log.warn("⚠️  Admin security is DISABLED - allowing access without token validation");
            return joinPoint.proceed();
        }

        // Get the current HTTP request
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            log.error("❌ No HTTP request context available for admin token validation");
            return buildForbiddenResponse("Invalid request context");
        }

        // Get the provided token from header
        String providedToken = request.getHeader(ADMIN_TOKEN_HEADER);

        // Validate token is provided
        if (providedToken == null || providedToken.trim().isEmpty()) {
            String clientIp = getClientIpAddress(request);
            log.warn("❌ Admin token missing from IP: {} - endpoint: {} {}",
                    clientIp, request.getMethod(), request.getRequestURI());
            return buildForbiddenResponse(requiresAdminToken.message());
        }

        // Validate configured token exists
        if (configuredAdminToken == null || configuredAdminToken.trim().isEmpty()) {
            log.error("❌ CRITICAL: Admin token not configured in application properties!");
            return buildForbiddenResponse("Admin authentication not configured");
        }

        // Validate token matches (constant-time comparison to prevent timing attacks)
        if (!constantTimeEquals(configuredAdminToken, providedToken)) {
            String clientIp = getClientIpAddress(request);
            log.warn("❌ Invalid admin token from IP: {} - endpoint: {} {}",
                    clientIp, request.getMethod(), request.getRequestURI());
            return buildForbiddenResponse(requiresAdminToken.message());
        }

        // Token is valid, proceed with method execution
        String clientIp = getClientIpAddress(request);
        log.info("✅ Admin token validated successfully from IP: {} - endpoint: {} {}",
                clientIp, request.getMethod(), request.getRequestURI());

        return joinPoint.proceed();
    }

    /**
     * Get the current HttpServletRequest from RequestContextHolder
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Get client IP address from request, handling X-Forwarded-For header
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Constant-time string comparison to prevent timing attacks
     * Compares every character even after finding a mismatch
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }

        // Length check (not constant time, but acceptable for token validation)
        if (a.length() != b.length()) {
            return false;
        }

        // Constant-time character comparison
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }

        return result == 0;
    }

    /**
     * Build a standardized 403 FORBIDDEN response
     */
    private ResponseEntity<Map<String, Object>> buildForbiddenResponse(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "status", "error",
                        "code", "FORBIDDEN",
                        "message", message,
                        "hint", "Provide valid admin token in " + ADMIN_TOKEN_HEADER + " header"
                ));
    }
}
