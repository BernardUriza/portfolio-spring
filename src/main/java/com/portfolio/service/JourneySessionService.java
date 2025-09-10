/**
 * Creado por Bernard Orozco
 * Service for managing journey sessions
 */
package com.portfolio.service;

import com.portfolio.model.JourneyEvent;
import com.portfolio.model.JourneySession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class JourneySessionService {
    
    private static final Logger logger = LoggerFactory.getLogger(JourneySessionService.class);
    
    @Autowired(required = false)
    private NarrationMetricsService metricsService;
    
    private final Cache<String, JourneySession> sessions = Caffeine.newBuilder()
            .maximumSize(2000)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();
    
    public JourneySession createSession() {
        JourneySession session = new JourneySession();
        sessions.put(session.getSessionId(), session);
        logger.info("Created new journey session: {}", session.getSessionId());
        
        if (metricsService != null) {
            metricsService.recordSessionCreated();
        }
        
        return session;
    }
    
    public JourneySession getSession(String sessionId) {
        JourneySession session = sessions.getIfPresent(sessionId);
        if (session != null && session.isExpired()) {
            sessions.invalidate(sessionId);
            logger.info("Expired session removed: {}", sessionId);
            return null;
        }
        return session;
    }
    
    public void addEvents(String sessionId, List<JourneyEvent> events) {
        JourneySession session = getSession(sessionId);
        if (session != null && events != null && !events.isEmpty()) {
            // Validate and filter events
            List<JourneyEvent> validEvents = events.stream()
                    .filter(this::isValidEvent)
                    .limit(50) // Max 50 events per request
                    .toList();
            
            session.addEvents(validEvents);
            sessions.put(sessionId, session);
            logger.debug("Added {} events to session: {}", validEvents.size(), sessionId);
            
            if (metricsService != null) {
                metricsService.recordEventsReceived(validEvents.size());
            }
        } else {
            logger.warn("Failed to add events - session not found or events empty: {}", sessionId);
        }
    }
    
    private boolean isValidEvent(JourneyEvent event) {
        return event != null 
                && event.getType() != null 
                && isValidEventType(event.getType())
                && event.getTs() > 0
                && event.getData() != null;
    }
    
    private boolean isValidEventType(String type) {
        return List.of("route", "project_view", "project_click", "project_hover", "heartbeat", "finish").contains(type);
    }
    
    public List<JourneyEvent> getRecentEvents(String sessionId, int count) {
        JourneySession session = getSession(sessionId);
        return session != null ? session.getRecentEvents(count) : List.of();
    }
    
    public void invalidateSession(String sessionId) {
        sessions.invalidate(sessionId);
        logger.info("Invalidated session: {}", sessionId);
    }
    
    public void purgeExpiredSessions() {
        sessions.cleanUp();
        logger.debug("Purged expired sessions, active count: {}", sessions.estimatedSize());
    }
    
    public long getActiveSessionCount() {
        return sessions.estimatedSize();
    }
}