/**
 * Creado por Bernard Orozco
 * DTO for contact message creation requests
 */
package com.portfolio.dto;

import jakarta.validation.constraints.*;

public class ContactMessageRequest {
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must be less than 150 characters")
    private String email;
    
    @Size(max = 150, message = "Company must be less than 150 characters")
    private String company;
    
    @NotBlank(message = "Subject is required")
    @Size(max = 150, message = "Subject must be less than 150 characters")
    private String subject;
    
    @NotBlank(message = "Message is required")
    @Size(max = 4000, message = "Message must be less than 4000 characters")
    private String message;
    
    @Size(max = 200, message = "Source path must be less than 200 characters")
    private String sourcePath;
    
    @Size(max = 64, message = "Session ID must be less than 64 characters")
    private String sessionId;
    
    private String honeypot;

    // Default constructor
    public ContactMessageRequest() {
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getHoneypot() {
        return honeypot;
    }

    public void setHoneypot(String honeypot) {
        this.honeypot = honeypot;
    }
}