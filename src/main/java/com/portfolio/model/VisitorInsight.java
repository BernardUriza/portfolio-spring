/**
 * Creado por Bernard Orozco
 * Entity for storing visitor journey insights
 */
package com.portfolio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Entity
@Table(name = "visitor_insights")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}