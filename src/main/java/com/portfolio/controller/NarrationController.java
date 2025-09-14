/**
 * Creado por Bernard Orozco
 * Controller for AI narration SSE stream
 */
package com.portfolio.controller;

import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import com.portfolio.service.ClaudeNarrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@ConditionalOnProperty(name = "app.narration.enabled", havingValue = "true")
@RequestMapping("/api/ai/narration")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:5173", "https://bernarduriza.github.io"})
public class NarrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NarrationController.class);
    
    @Autowired
    private ClaudeNarrationService narrationService;
    
    @Autowired
    private PortfolioProjectJpaRepository portfolioProjectRepository;
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNarration(@RequestParam String sessionId, 
                                     HttpServletRequest request, 
                                     HttpServletResponse response) throws IOException {
        String clientIp = getClientIp(request);
        
        logger.info("Starting narration stream for session: {} from IP: {}", sessionId, clientIp);
        
        // Set proper SSE headers
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");
        
        // Gate: Check if portfolio is empty
        long portfolioCount = portfolioProjectRepository.count();
        if (portfolioCount == 0) {
            logger.warn("Narration stream blocked - portfolio is empty (count={})", portfolioCount);
            SseEmitter errorEmitter = new SseEmitter(5000L);
            try {
                errorEmitter.send(SseEmitter.event()
                    .name("error")
                    .data(Map.of("code", "portfolio-empty", 
                               "message", "Portfolio is empty. Sync in progress. Try again soon.")));
                errorEmitter.complete();
            } catch (IOException e) {
                logger.error("Failed to send portfolio-empty error", e);
            }
            return errorEmitter;
        }
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.warn("Invalid sessionId provided");
            return createErrorStream("Invalid session ID");
        }
        
        // Create narration stream using service
        SseEmitter emitter = narrationService.createNarrationStream(sessionId, clientIp);
        
        if (emitter == null) {
            logger.warn("Failed to create narration stream for session: {}", sessionId);
            return createErrorStream("Failed to create narration stream");
        }
        
        return emitter;
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
