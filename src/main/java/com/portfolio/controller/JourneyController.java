/**
 * Creado por Bernard Orozco
 * REST controller for journey tracking
 */
package com.portfolio.controller;

import com.portfolio.dto.JourneyEventRequest;
import com.portfolio.dto.SessionResponse;
import com.portfolio.model.JourneySession;
import com.portfolio.service.JourneySessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/journey")
@CrossOrigin(origins = {"http://localhost:4200", "https://bernarduriza.github.io"})
public class JourneyController {
    
    private static final Logger logger = LoggerFactory.getLogger(JourneyController.class);
    
    @Autowired
    private JourneySessionService journeySessionService;
    
    // Rate limiting buckets per IP
    private final Map<String, Bucket> rateLimitBuckets = new ConcurrentHashMap<>();
    
    @PostMapping("/session")
    public ResponseEntity<SessionResponse> createSession(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        
        if (!isAllowed(clientIp, "session", 10)) { // 10 per minute
            logger.warn("Rate limit exceeded for session creation from IP: {}", clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        
        try {
            JourneySession session = journeySessionService.createSession();
            SessionResponse response = new SessionResponse(
                session.getSessionId(),
                session.getExpiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            logger.info("Created session {} for IP: {}", session.getSessionId(), clientIp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/event")
    public ResponseEntity<Void> addEvents(@RequestBody JourneyEventRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        
        if (!isAllowed(clientIp, "event", 60)) { // 60 per minute
            logger.warn("Rate limit exceeded for events from IP: {}", clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        
        if (request.getSessionId() == null || request.getEvents() == null || request.getEvents().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        if (request.getEvents().size() > 50) {
            logger.warn("Too many events in single request: {} from IP: {}", request.getEvents().size(), clientIp);
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }
        
        try {
            journeySessionService.addEvents(request.getSessionId(), request.getEvents());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error adding events for session: " + request.getSessionId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private boolean isAllowed(String clientIp, String type, int capacity) {
        String key = clientIp + "_" + type;
        Bucket bucket = rateLimitBuckets.computeIfAbsent(key, k -> createBucket(capacity));
        return bucket.tryConsume(1);
    }
    
    private Bucket createBucket(int capacity) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}