package com.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for project history operations
 * Provides methods for querying and managing project version history
 *
 * @author Bernard Uriza Orozco
 * @since 2025-10-27
 */
@Repository
public interface ProjectHistoryJpaRepository extends JpaRepository<ProjectHistoryJpaEntity, Long> {

    /**
     * Find all history entries for a specific project, ordered by version descending
     */
    List<ProjectHistoryJpaEntity> findByProjectIdOrderByVersionNumberDesc(Long projectId);

    /**
     * Find all history entries for a project with pagination
     */
    Page<ProjectHistoryJpaEntity> findByProjectIdOrderByVersionNumberDesc(Long projectId, Pageable pageable);

    /**
     * Find a specific version of a project
     */
    Optional<ProjectHistoryJpaEntity> findByProjectIdAndVersionNumber(Long projectId, Integer versionNumber);

    /**
     * Find the latest version number for a project
     */
    @Query("SELECT COALESCE(MAX(h.versionNumber), 0) FROM ProjectHistoryJpaEntity h WHERE h.projectId = :projectId")
    Integer findLatestVersionNumber(@Param("projectId") Long projectId);

    /**
     * Find the most recent history entry for a project
     */
    Optional<ProjectHistoryJpaEntity> findTopByProjectIdOrderByVersionNumberDesc(Long projectId);

    /**
     * Find history entries by change type
     */
    List<ProjectHistoryJpaEntity> findByProjectIdAndChangeTypeOrderByVersionNumberDesc(
            Long projectId, ProjectHistoryJpaEntity.ChangeType changeType);

    /**
     * Find history entries within a date range
     */
    List<ProjectHistoryJpaEntity> findByProjectIdAndCreatedAtBetweenOrderByVersionNumberDesc(
            Long projectId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find history entries by who made the change
     */
    List<ProjectHistoryJpaEntity> findByProjectIdAndChangedByOrderByVersionNumberDesc(
            Long projectId, String changedBy);

    /**
     * Count total versions for a project
     */
    Long countByProjectId(Long projectId);

    /**
     * Delete all history entries for a project (cascade delete)
     */
    void deleteByProjectId(Long projectId);

    /**
     * Find history entries where a specific field changed
     * Using native query to support PostgreSQL array operations
     */
    @Query(value = "SELECT * FROM project_history WHERE project_id = :projectId AND :fieldName = ANY(changed_fields) ORDER BY version_number DESC", nativeQuery = true)
    List<ProjectHistoryJpaEntity> findByProjectIdAndChangedField(@Param("projectId") Long projectId, @Param("fieldName") String fieldName);

    /**
     * Check if a version exists for a project
     */
    boolean existsByProjectIdAndVersionNumber(Long projectId, Integer versionNumber);
}
