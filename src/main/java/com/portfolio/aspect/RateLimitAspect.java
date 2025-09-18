package com.portfolio.aspect;

import com.portfolio.service.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Aspect
@Component
public class RateLimitAspect {
    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);
    private final RateLimitingService rateLimitingService;

    public RateLimitAspect(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }
    
    @Around("@annotation(rateLimit)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        
        // Get client IP address
        String clientId = getClientIpAddress();
        
        // Check rate limit
        if (!rateLimitingService.isWithinRateLimit(clientId, rateLimit.type())) {
            long timeUntilReset = rateLimitingService.getTimeUntilReset(clientId, rateLimit.type());
            int remaining = rateLimitingService.getRemainingRequests(clientId, rateLimit.type());
            
            log.warn("Rate limit exceeded for client {} on {} operation", clientId, rateLimit.type());
            
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Limit", String.valueOf(getRateLimitForType(rateLimit.type())))
                .header("X-RateLimit-Remaining", String.valueOf(remaining))
                .header("X-RateLimit-Reset", String.valueOf(timeUntilReset))
                .body(Map.of(
                    "error", "Rate limit exceeded",
                    "message", "Too many requests for " + rateLimit.type().name().toLowerCase().replace("_", " "),
                    "retryAfterSeconds", timeUntilReset,
                    "rateLimitType", rateLimit.type().name()
                ));
        }
        
        // Proceed with the method execution
        Object result = joinPoint.proceed();
        
        // Add rate limit headers to successful responses
        if (result instanceof ResponseEntity<?> responseEntity) {
            int remaining = rateLimitingService.getRemainingRequests(clientId, rateLimit.type());
            long timeUntilReset = rateLimitingService.getTimeUntilReset(clientId, rateLimit.type());
            
            return ResponseEntity.status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .header("X-RateLimit-Limit", String.valueOf(getRateLimitForType(rateLimit.type())))
                .header("X-RateLimit-Remaining", String.valueOf(remaining))
                .header("X-RateLimit-Reset", String.valueOf(timeUntilReset))
                .body(responseEntity.getBody());
        }
        
        return result;
    }
    
    private String getClientIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // Check for X-Forwarded-For header
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Check for X-Real-IP header
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Default to remote address
        return request.getRemoteAddr();
    }
    
    private int getRateLimitForType(RateLimitingService.RateLimitType type) {
        return switch (type) {
            case ADMIN_ENDPOINTS -> 60;
            case FACTORY_RESET -> 1;
            case SYNC_OPERATIONS -> 10;
            case AI_CURATION -> 30;
        };
    }
}
