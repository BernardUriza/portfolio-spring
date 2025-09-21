/**
 * Creado por Bernard Orozco
 * Repository for VisitorInsight entity
 */
package com.portfolio.repository;

import com.portfolio.model.VisitorInsight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VisitorInsightRepository extends JpaRepository<VisitorInsight, Long> {
    
    Optional<VisitorInsight> findBySessionId(String sessionId);
    
    @Query("SELECT vi FROM VisitorInsight vi WHERE " +
           "(CAST(:dateFrom AS timestamp) IS NULL OR vi.startedAt >= CAST(:dateFrom AS timestamp)) AND " +
           "(CAST(:dateTo AS timestamp) IS NULL OR vi.startedAt <= CAST(:dateTo AS timestamp)) AND " +
           "(CAST(:minDuration AS integer) IS NULL OR vi.durationSeconds >= CAST(:minDuration AS integer)) AND " +
           "(CAST(:hasContact AS boolean) IS NULL OR " +
           "  (CAST(:hasContact AS boolean) = true AND vi.contactMessageId IS NOT NULL) OR " +
           "  (CAST(:hasContact AS boolean) = false AND vi.contactMessageId IS NULL))")
    Page<VisitorInsight> findWithFilters(
        @Param("dateFrom") LocalDateTime dateFrom,
        @Param("dateTo") LocalDateTime dateTo,
        @Param("minDuration") Integer minDuration,
        @Param("hasContact") Boolean hasContact,
        Pageable pageable
    );
    
    @Query("SELECT vi FROM VisitorInsight vi WHERE vi.contactMessageId = :contactMessageId")
    Optional<VisitorInsight> findByContactMessageId(@Param("contactMessageId") Long contactMessageId);
    
    @Query("SELECT COUNT(vi) FROM VisitorInsight vi WHERE vi.createdAt >= :since")
    long countInsightsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(vi.durationSeconds) FROM VisitorInsight vi WHERE vi.durationSeconds IS NOT NULL")
    Double getAverageSessionDuration();
}