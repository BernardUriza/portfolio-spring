/**
 * Creado por Bernard Orozco
 * Entity for storing visitor journey insights
 */
package com.portfolio.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "visitor_insights")
public class VisitorInsight {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", unique = true, nullable = false, length = 64)
    private String sessionId;
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "pages_visited")
    private Integer pagesVisited = 0;
    
    @ElementCollection
    @CollectionTable(name = "visitor_insight_projects", joinColumns = @JoinColumn(name = "insight_id"))
    @Column(name = "project_repo")
    private List<String> projectsViewed = new ArrayList<>();
    
    @Column(name = "actions", columnDefinition = "TEXT")
    private String actions; // JSON string of action summary
    
    @Column(name = "ai_conclusion", length = 4000)
    private String aiConclusion;
    
    @Column(name = "contact_message_id")
    private Long contactMessageId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
    
    public void calculateDuration() {
        if (startedAt != null && endedAt != null) {
            durationSeconds = (int) java.time.Duration.between(startedAt, endedAt).getSeconds();
        }
    }

    // Default constructor
    public VisitorInsight() {
    }

    // All args constructor
    public VisitorInsight(Long id, String sessionId, LocalDateTime startedAt, LocalDateTime endedAt,
                          Integer durationSeconds, Integer pagesVisited, List<String> projectsViewed,
                          String actions, String aiConclusion, Long contactMessageId, LocalDateTime createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationSeconds = durationSeconds;
        this.pagesVisited = pagesVisited != null ? pagesVisited : 0;
        this.projectsViewed = projectsViewed != null ? projectsViewed : new ArrayList<>();
        this.actions = actions;
        this.aiConclusion = aiConclusion;
        this.contactMessageId = contactMessageId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getPagesVisited() {
        return pagesVisited;
    }

    public void setPagesVisited(Integer pagesVisited) {
        this.pagesVisited = pagesVisited != null ? pagesVisited : 0;
    }

    public List<String> getProjectsViewed() {
        return projectsViewed;
    }

    public void setProjectsViewed(List<String> projectsViewed) {
        this.projectsViewed = projectsViewed != null ? projectsViewed : new ArrayList<>();
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getAiConclusion() {
        return aiConclusion;
    }

    public void setAiConclusion(String aiConclusion) {
        this.aiConclusion = aiConclusion;
    }

    public Long getContactMessageId() {
        return contactMessageId;
    }

    public void setContactMessageId(Long contactMessageId) {
        this.contactMessageId = contactMessageId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
