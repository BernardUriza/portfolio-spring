package com.portfolio.adapter.in.rest;

import com.portfolio.adapter.in.rest.dto.FactoryResetResponseDto;
import com.portfolio.adapter.in.rest.dto.ResetAuditDto;
import com.portfolio.adapter.in.rest.mapper.ResetAuditRestMapper;
import com.portfolio.core.domain.admin.ResetAudit;
import com.portfolio.service.FactoryResetService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200"}, allowedHeaders = "*")
public class AdminResetController {
    
    private final FactoryResetService factoryResetService;
    private final ResetAuditRestMapper resetAuditMapper;
    
    @Value("${app.admin.factory-reset.enabled:false}")
    private boolean factoryResetEnabled;
    
    @Value("${app.admin.factory-reset.token:}")
    private String adminResetToken;
    
    // Rate limiting: 1 request per 10 minutes per IP
    private final ConcurrentHashMap<String, Bucket> rateLimitBuckets = new ConcurrentHashMap<>();
    
    @PostMapping("/factory-reset")
    public ResponseEntity<FactoryResetResponseDto> startFactoryReset(
            HttpServletRequest request,
            @RequestHeader(value = "X-Admin-Reset-Token", required = false) String providedToken,
            @RequestHeader(value = "X-Admin-Confirm", required = false) String confirmHeader) {
        
        // Check if factory reset is enabled
        if (!factoryResetEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Factory reset is disabled");
        }
        
        // Validate admin token
        if (adminResetToken.isEmpty() || !adminResetToken.equals(providedToken)) {
            log.warn("Invalid or missing admin reset token from IP: {}", getClientIpAddress(request));
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin reset token");
        }
        
        // Double-check confirmation header
        if (!"DELETE".equals(confirmHeader)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Missing or invalid confirmation header. X-Admin-Confirm must be 'DELETE'");
        }
        
        // Rate limiting
        String clientIp = getClientIpAddress(request);
        if (!isRequestAllowed(clientIp)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, 
                "Rate limit exceeded. Only 1 factory reset attempt allowed per 10 minutes per IP");
        }
        
        try {
            // Check for active jobs
            List<ResetAudit> activeJobs = factoryResetService.getActiveJobs();
            if (!activeJobs.isEmpty()) {
                ResetAudit activeJob = activeJobs.get(0);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(FactoryResetResponseDto.builder()
                        .jobId(activeJob.getJobId())
                        .message("Factory reset already in progress")
                        .streamUrl("/api/admin/factory-reset/stream/" + activeJob.getJobId())
                        .build());
            }
            
            // Start factory reset
            ResetAudit resetAudit = factoryResetService.startFactoryReset("admin", clientIp);
            
            log.info("Factory reset initiated by admin from IP: {}, Job ID: {}", clientIp, resetAudit.getJobId());
            
            return ResponseEntity.accepted()
                .body(FactoryResetResponseDto.builder()
                    .jobId(resetAudit.getJobId())
                    .message("Factory reset started successfully")
                    .streamUrl("/api/admin/factory-reset/stream/" + resetAudit.getJobId())
                    .build());
                    
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to start factory reset from IP: {}", clientIp, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to start factory reset: " + e.getMessage());
        }
    }
    
    @GetMapping("/factory-reset/stream/{jobId}")
    public SseEmitter streamFactoryResetProgress(@PathVariable String jobId) {
        // Check if factory reset is enabled
        if (!factoryResetEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Factory reset is disabled");
        }
        
        // Verify job exists
        ResetAudit resetAudit = factoryResetService.getResetAuditByJobId(jobId);
        if (resetAudit == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reset job not found: " + jobId);
        }
        
        log.info("Starting SSE stream for factory reset job: {}", jobId);
        return factoryResetService.streamResetProgress(jobId);
    }
    
    @GetMapping("/factory-reset/audit")
    public ResponseEntity<List<ResetAuditDto>> getFactoryResetAudit(
            @RequestParam(defaultValue = "20") int limit) {
        
        // Check if factory reset is enabled
        if (!factoryResetEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Factory reset is disabled");
        }
        
        List<ResetAuditDto> auditHistory = factoryResetService.getResetHistory(limit)
            .stream()
            .map(resetAuditMapper::toRestDto)
            .toList();
            
        return ResponseEntity.ok(auditHistory);
    }
    
    private boolean isRequestAllowed(String clientIp) {
        Bucket bucket = rateLimitBuckets.computeIfAbsent(clientIp, this::createNewBucket);
        return bucket.tryConsume(1);
    }
    
    private Bucket createNewBucket(String clientIp) {
        // 1 request per 10 minutes
        Bandwidth limit = Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(10)));
        return Bucket4j.builder()
            .addLimit(limit)
            .build();
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}