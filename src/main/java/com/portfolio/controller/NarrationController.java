/**
 * Creado por Bernard Orozco
 * Controller for AI narration SSE stream
 */
package com.portfolio.controller;

import com.portfolio.service.ClaudeNarrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/ai/narration")
@CrossOrigin(origins = {"http://localhost:4200", "https://bernarduriza.github.io"})
public class NarrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NarrationController.class);
    
    @Autowired
    private ClaudeNarrationService narrationService;
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNarration(@RequestParam String sessionId, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        
        logger.info("Starting narration stream for session: {} from IP: {}", sessionId, clientIp);
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.warn("Invalid sessionId provided");
            return createErrorStream("Invalid session ID");
        }
        
        try {
            SseEmitter emitter = narrationService.createNarrationStream(sessionId, clientIp);
            
            if (emitter == null) {
                logger.warn("Failed to create narration stream for session: {}", sessionId);
                return createErrorStream("Failed to create stream");
            }
            
            return emitter;
            
        } catch (Exception e) {
            logger.error("Error creating narration stream", e);
            return createErrorStream("Internal server error");
        }
    }
    
    private SseEmitter createErrorStream(String errorMessage) {
        SseEmitter emitter = new SseEmitter(5000L);
        try {
            emitter.send(SseEmitter.event().data("ERROR:" + errorMessage));
            emitter.complete();
        } catch (Exception e) {
            logger.error("Failed to send error message", e);
        }
        return emitter;
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