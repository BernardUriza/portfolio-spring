/**
 * Creado por Bernard Orozco
 * Ephemeral session for visitor journey tracking
 */
package com.portfolio.model;

import java.time.LocalDateTime;
import java.util.*;

public class JourneySession {
    private String sessionId;
    private LocalDateTime startedAt;
    private LocalDateTime lastEventAt;
    private List<JourneyEvent> events;
    private boolean muted;
    
    public JourneySession() {
        this.sessionId = UUID.randomUUID().toString();
        this.startedAt = LocalDateTime.now();
        this.lastEventAt = LocalDateTime.now();
        this.events = new ArrayList<>();
        this.muted = false;
    }
    
    public void addEvent(JourneyEvent event) {
        this.events.add(event);
        this.lastEventAt = LocalDateTime.now();
        
        // Keep only last 50 events per session
        if (this.events.size() > 50) {
            this.events = new ArrayList<>(this.events.subList(this.events.size() - 50, this.events.size()));
        }
    }
    
    public void addEvents(List<JourneyEvent> newEvents) {
        this.events.addAll(newEvents);
        this.lastEventAt = LocalDateTime.now();
        
        // Keep only last 50 events per session
        if (this.events.size() > 50) {
            this.events = new ArrayList<>(this.events.subList(this.events.size() - 50, this.events.size()));
        }
    }
    
    // Getters and setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getLastEventAt() {
        return lastEventAt;
    }
    
    public void setLastEventAt(LocalDateTime lastEventAt) {
        this.lastEventAt = lastEventAt;
    }
    
    public List<JourneyEvent> getEvents() {
        return events;
    }
    
    public void setEvents(List<JourneyEvent> events) {
        this.events = events;
    }
    
    public boolean isMuted() {
        return muted;
    }
    
    public void setMuted(boolean muted) {
        this.muted = muted;
    }
    
    public LocalDateTime getExpiresAt() {
        return startedAt.plusMinutes(30); // 30 minute TTL
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(getExpiresAt());
    }
    
    public List<JourneyEvent> getRecentEvents(int count) {
        if (events.isEmpty()) return new ArrayList<>();
        int startIndex = Math.max(0, events.size() - count);
        return new ArrayList<>(events.subList(startIndex, events.size()));
    }
}