/**
 * Creado por Bernard Orozco
 * Entity for storing contact form messages
 */
package com.portfolio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "contact_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must be less than 150 characters")
    @Column(name = "email", nullable = false, length = 150)
    private String email;
    
    @Size(max = 150, message = "Company must be less than 150 characters")
    @Column(name = "company", length = 150)
    private String company;
    
    @NotBlank(message = "Subject is required")
    @Size(max = 150, message = "Subject must be less than 150 characters")
    @Column(name = "subject", nullable = false, length = 150)
    private String subject;
    
    @NotBlank(message = "Message is required")
    @Size(max = 4000, message = "Message must be less than 4000 characters")
    @Column(name = "message", nullable = false, length = 4000)
    private String message;
    
    @Size(max = 200, message = "Source path must be less than 200 characters")
    @Column(name = "source_path", length = 200)
    private String sourcePath;
    
    @Size(max = 64, message = "Session ID must be less than 64 characters")
    @Column(name = "session_id", length = 64)
    private String sessionId;
    
    @Size(max = 400, message = "User agent must be less than 400 characters")
    @Column(name = "user_agent", length = 400)
    private String userAgent;
    
    @Size(max = 64, message = "IP hash must be less than 64 characters")
    @Column(name = "ip_hash", length = 64)
    private String ipHash;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status = MessageStatus.NEW;
    
    @ElementCollection
    @CollectionTable(name = "contact_message_labels", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "label")
    private List<String> labels = new ArrayList<>();
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @JsonIgnore
    @Column(name = "honeypot")
    private String honeypot;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum MessageStatus {
        NEW, IN_REVIEW, RESOLVED, ARCHIVED
    }
}