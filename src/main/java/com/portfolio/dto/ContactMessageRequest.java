/**
 * Creado por Bernard Orozco
 * DTO for contact message creation requests
 */
package com.portfolio.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
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
}