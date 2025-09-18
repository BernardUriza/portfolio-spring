/**
 * Creado por Bernard Orozco
 * DTO for contact message creation responses
 */
package com.portfolio.dto;

import java.time.LocalDateTime;

public class ContactMessageResponse {
    private Long id;
    private LocalDateTime createdAt;

    // Default constructor
    public ContactMessageResponse() {
    }

    // All args constructor
    public ContactMessageResponse(Long id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}