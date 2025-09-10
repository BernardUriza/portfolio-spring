/**
 * Creado por Bernard Orozco
 * Request DTO for journey events
 */
package com.portfolio.dto;

import com.portfolio.model.JourneyEvent;
import java.util.List;

public class JourneyEventRequest {
    private String sessionId;
    private List<JourneyEvent> events;
    
    public JourneyEventRequest() {}
    
    public JourneyEventRequest(String sessionId, List<JourneyEvent> events) {
        this.sessionId = sessionId;
        this.events = events;
    }
    
    // Getters and setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public List<JourneyEvent> getEvents() {
        return events;
    }
    
    public void setEvents(List<JourneyEvent> events) {
        this.events = events;
    }
}