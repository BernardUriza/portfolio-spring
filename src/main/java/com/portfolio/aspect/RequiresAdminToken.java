package com.portfolio.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark controller methods that require admin token validation
 *
 * Usage:
 * <pre>
 * {@code
 * @PostMapping("/api/admin/some-action")
 * @RequiresAdminToken
 * public ResponseEntity<?> adminAction() {
 *     // Only accessible with valid X-Admin-Token header
 * }
 * }
 * </pre>
 *
 * The aspect will validate the X-Admin-Token header against portfolio.admin.token
 * configuration property. Returns 403 FORBIDDEN if token is missing or invalid.
 *
 * Created as part of SEC-002: Add Admin Token Validation
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 * @see AdminTokenAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAdminToken {
    /**
     * Optional custom error message when token validation fails
     */
    String message() default "Valid admin token required";
}
