package com.portfolio.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Entity for project history tracking
 * Stores complete snapshots of portfolio projects for versioning and rollback
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 */
@Entity
@Table(name = "project_history",
        indexes = {
                @Index(name = "idx_project_history_project_id", columnList = "project_id"),
                @Index(name = "idx_project_history_created_at", columnList = "created_at"),
                @Index(name = "idx_project_history_version", columnList = "project_id, version_number"),
        })
public class ProjectHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    /**
     * Complete JSON snapshot of the project at this version
     * Uses PostgreSQL JSONB type for efficient storage and querying
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_data", nullable = false, columnDefinition = "jsonb")
    private String snapshotData;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", length = 50, nullable = false)
    private ChangeType changeType;

    /**
     * Array of field names that changed in this version
     * Stored as PostgreSQL TEXT[] for efficient querying
     */
    @Column(name = "changed_fields", columnDefinition = "text[]")
    private List<String> changedFields;

    @Column(name = "changed_by", length = 255)
    private String changedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Types of changes that can be recorded in project history
     */
    public enum ChangeType {
        /**
         * Initial project creation
         */
        CREATE,

        /**
         * Manual update by user
         */
        UPDATE,

        /**
         * Project deletion (soft delete)
         */
        DELETE,

        /**
         * Rollback to previous version
         */
        ROLLBACK,

        /**
         * Automatic sync from GitHub
         */
        SYNC,

        /**
         * Manual override during sync
         */
        MANUAL
    }

    // Constructors
    public ProjectHistoryJpaEntity() {}

    public ProjectHistoryJpaEntity(Long projectId, Integer versionNumber, String snapshotData,
                                   ChangeType changeType, List<String> changedFields, String changedBy) {
        this.projectId = projectId;
        this.versionNumber = versionNumber;
        this.snapshotData = snapshotData;
        this.changeType = changeType;
        this.changedFields = changedFields;
        this.changedBy = changedBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getSnapshotData() {
        return snapshotData;
    }

    public void setSnapshotData(String snapshotData) {
        this.snapshotData = snapshotData;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public List<String> getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(List<String> changedFields) {
        this.changedFields = changedFields;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long projectId;
        private Integer versionNumber;
        private String snapshotData;
        private ChangeType changeType;
        private List<String> changedFields;
        private String changedBy;

        public Builder projectId(Long projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder versionNumber(Integer versionNumber) {
            this.versionNumber = versionNumber;
            return this;
        }

        public Builder snapshotData(String snapshotData) {
            this.snapshotData = snapshotData;
            return this;
        }

        public Builder changeType(ChangeType changeType) {
            this.changeType = changeType;
            return this;
        }

        public Builder changedFields(List<String> changedFields) {
            this.changedFields = changedFields;
            return this;
        }

        public Builder changedBy(String changedBy) {
            this.changedBy = changedBy;
            return this;
        }

        public ProjectHistoryJpaEntity build() {
            return new ProjectHistoryJpaEntity(projectId, versionNumber, snapshotData,
                    changeType, changedFields, changedBy);
        }
    }

    /**
     * Check if this version represents a user-initiated change
     */
    public boolean isUserInitiated() {
        return changeType == ChangeType.UPDATE ||
               changeType == ChangeType.MANUAL ||
               changeType == ChangeType.CREATE;
    }

    /**
     * Check if this version was created by sync
     */
    public boolean isSyncGenerated() {
        return changeType == ChangeType.SYNC;
    }

    /**
     * Check if this is a rollback version
     */
    public boolean isRollback() {
        return changeType == ChangeType.ROLLBACK;
    }
}
