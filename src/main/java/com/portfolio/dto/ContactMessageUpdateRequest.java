/**
 * Creado por Bernard Orozco
 * DTO for contact message admin updates
 */
package com.portfolio.dto;

import com.portfolio.model.ContactMessage.MessageStatus;

import java.util.List;

public class ContactMessageUpdateRequest {
    private MessageStatus status;
    private List<String> labels;
    private String notes;

    // Default constructor
    public ContactMessageUpdateRequest() {
    }

    // Getters and Setters
    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}