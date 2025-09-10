/**
 * Creado por Bernard Orozco
 * Response DTO for journey session
 */
package com.portfolio.dto;

import java.time.LocalDateTime;

public class SessionResponse {
    private String sessionId;
    private String expiresAt;
    
    public SessionResponse() {}
    
    public SessionResponse(String sessionId, String expiresAt) {
        this.sessionId = sessionId;
        this.expiresAt = expiresAt;
    }
    
    // Getters and setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}