package com.portfolio.exception;

/**
 * Standardized error codes for API responses
 * Enables client-side error handling and retry logic
 *
 * Format: CATEGORY_SPECIFIC_ERROR
 * - VALIDATION_*: Input validation failures (4xx)
 * - AUTH_*: Authentication/authorization failures (401/403)
 * - RESOURCE_*: Resource access failures (404)
 * - RATE_LIMIT_*: Rate limiting violations (429)
 * - SERVER_*: Internal server errors (5xx)
 * - EXTERNAL_*: External service failures (502/503)
 *
 * Created as part of API-001: RFC 7807 Error Responses
 * Aligns with Catalytic Architecture principle of Transparency
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-28
 */
public enum ErrorCode {

    // Validation Errors (400)
    VALIDATION_FAILED("validation_failed", "Input validation failed", true),
    VALIDATION_FIELD_INVALID("validation_field_invalid", "Invalid field value", true),
    VALIDATION_MISSING_REQUIRED("validation_missing_required", "Required field missing", true),
    VALIDATION_CONSTRAINT_VIOLATION("validation_constraint_violation", "Constraint violation", true),

    // Authentication Errors (401)
    AUTH_TOKEN_MISSING("auth_token_missing", "Authentication token missing", true),
    AUTH_TOKEN_INVALID("auth_token_invalid", "Authentication token invalid", false),
    AUTH_TOKEN_EXPIRED("auth_token_expired", "Authentication token expired", true),

    // Authorization Errors (403)
    AUTH_INSUFFICIENT_PERMISSIONS("auth_insufficient_permissions", "Insufficient permissions", false),
    AUTH_FEATURE_DISABLED("auth_feature_disabled", "Feature disabled", false),
    AUTH_ADMIN_REQUIRED("auth_admin_required", "Admin access required", false),

    // Resource Errors (404)
    RESOURCE_NOT_FOUND("resource_not_found", "Resource not found", false),
    RESOURCE_DELETED("resource_deleted", "Resource deleted", false),

    // Conflict Errors (409)
    RESOURCE_CONFLICT("resource_conflict", "Resource conflict", true),
    RESOURCE_ALREADY_EXISTS("resource_already_exists", "Resource already exists", false),

    // Rate Limiting (429)
    RATE_LIMIT_EXCEEDED("rate_limit_exceeded", "Rate limit exceeded", true),
    RATE_LIMIT_TOKEN_BUDGET_EXCEEDED("rate_limit_token_budget", "Token budget exceeded", true),

    // Server Errors (500)
    SERVER_INTERNAL_ERROR("server_internal_error", "Internal server error", true),
    SERVER_CONFIGURATION_ERROR("server_configuration_error", "Server configuration error", false),
    SERVER_DATABASE_ERROR("server_database_error", "Database error", true),

    // External Service Errors (502/503)
    EXTERNAL_SERVICE_UNAVAILABLE("external_service_unavailable", "External service unavailable", true),
    EXTERNAL_GITHUB_API_ERROR("external_github_error", "GitHub API error", true),
    EXTERNAL_CLAUDE_API_ERROR("external_claude_error", "Claude API error", true),
    EXTERNAL_SERVICE_TIMEOUT("external_service_timeout", "External service timeout", true);

    private final String code;
    private final String description;
    private final boolean retryable;

    ErrorCode(String code, String description, boolean retryable) {
        this.code = code;
        this.description = description;
        this.retryable = retryable;
    }

    /**
     * Machine-readable error code (e.g., "validation_failed")
     */
    public String getCode() {
        return code;
    }

    /**
     * Human-readable description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Whether the client should retry the request
     * - true: Retry may succeed (transient error)
     * - false: Retry will fail (permanent error, requires client changes)
     */
    public boolean isRetryable() {
        return retryable;
    }
}
