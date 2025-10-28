package com.portfolio.exception;

import com.portfolio.adapter.in.rest.dto.ProblemDetailDto;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler implementing RFC 7807 Problem Details
 *
 * Provides standardized, machine-readable error responses across all endpoints.
 * Aligns with Catalytic Architecture principle of Transparency.
 *
 * Updated as part of API-001: RFC 7807 Error Responses
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-28
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle resource not found exceptions (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetailDto> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {} - URI: {}", ex.getMessage(), request.getRequestURI());

        ProblemDetailDto problem = ProblemDetailDto.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode(ErrorCode.RESOURCE_NOT_FOUND)
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Handle validation errors (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailDto> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing + "; " + replacement
                ));

        String detail = fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {} - URI: {}", detail, request.getRequestURI());

        ProblemDetailDto problem = ProblemDetailDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(ErrorCode.VALIDATION_FAILED)
                .detail(detail)
                .instance(request.getRequestURI())
                .additionalInfo("fields", fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Handle Spring ResponseStatusException (various status codes)
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetailDto> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ErrorCode errorCode = mapHttpStatusToErrorCode(status);

        log.warn("ResponseStatusException: {} - Status: {} - URI: {}",
                ex.getReason(), status, request.getRequestURI());

        ProblemDetailDto problem = ProblemDetailDto.builder()
                .status(status.value())
                .errorCode(errorCode)
                .detail(ex.getReason() != null ? ex.getReason() : status.getReasonPhrase())
                .instance(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(problem);
    }

    /**
     * Handle generic runtime exceptions (500)
     * SECURITY: Never expose internal details to clients
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetailDto> handleRuntime(
            RuntimeException ex,
            HttpServletRequest request) {

        // Log the full stack trace for debugging
        log.error("Internal server error - URI: {}", request.getRequestURI(), ex);

        // Return generic message to client (security best practice)
        ProblemDetailDto problem = ProblemDetailDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode(ErrorCode.SERVER_INTERNAL_ERROR)
                .detail("An internal error occurred. Please try again later or contact support if the problem persists.")
                .instance(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    /**
     * Handle all other exceptions (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailDto> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error - URI: {}", request.getRequestURI(), ex);

        ProblemDetailDto problem = ProblemDetailDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode(ErrorCode.SERVER_INTERNAL_ERROR)
                .detail("An unexpected error occurred. Please try again later.")
                .instance(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    /**
     * Map HTTP status codes to appropriate ErrorCode enums
     */
    private ErrorCode mapHttpStatusToErrorCode(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> ErrorCode.RESOURCE_NOT_FOUND;
            case FORBIDDEN -> ErrorCode.AUTH_INSUFFICIENT_PERMISSIONS;
            case UNAUTHORIZED -> ErrorCode.AUTH_TOKEN_INVALID;
            case BAD_REQUEST -> ErrorCode.VALIDATION_FAILED;
            case CONFLICT -> ErrorCode.RESOURCE_CONFLICT;
            case TOO_MANY_REQUESTS -> ErrorCode.RATE_LIMIT_EXCEEDED;
            case SERVICE_UNAVAILABLE -> ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE;
            case BAD_GATEWAY -> ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE;
            default -> ErrorCode.SERVER_INTERNAL_ERROR;
        };
    }
}
