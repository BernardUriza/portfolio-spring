/**
 * Creado por Bernard Orozco
 * Entity for storing contact form messages
 */
package com.portfolio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "contact_messages")
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

    // Default constructor
    public ContactMessage() {
    }

    // All args constructor
    public ContactMessage(Long id, String name, String email, String company, String subject, String message,
                          String sourcePath, String sessionId, String userAgent, String ipHash,
                          MessageStatus status, List<String> labels, String notes, LocalDateTime createdAt,
                          LocalDateTime updatedAt, String honeypot) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.company = company;
        this.subject = subject;
        this.message = message;
        this.sourcePath = sourcePath;
        this.sessionId = sessionId;
        this.userAgent = userAgent;
        this.ipHash = ipHash;
        this.status = status != null ? status : MessageStatus.NEW;
        this.labels = labels != null ? labels : new ArrayList<>();
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.honeypot = honeypot;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpHash() {
        return ipHash;
    }

    public void setIpHash(String ipHash) {
        this.ipHash = ipHash;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status != null ? status : MessageStatus.NEW;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels != null ? labels : new ArrayList<>();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getHoneypot() {
        return honeypot;
    }

    public void setHoneypot(String honeypot) {
        this.honeypot = honeypot;
    }

    public enum MessageStatus {
        NEW, IN_REVIEW, RESOLVED, ARCHIVED
    }
}