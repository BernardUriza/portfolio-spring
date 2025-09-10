/**
 * Creado por Bernard Orozco
 * Repository for ContactMessage entity
 */
package com.portfolio.repository;

import com.portfolio.model.ContactMessage;
import com.portfolio.model.ContactMessage.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    
    @Query("SELECT cm FROM ContactMessage cm WHERE " +
           "(:status IS NULL OR cm.status = :status) AND " +
           "(:query IS NULL OR LOWER(cm.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                   LOWER(cm.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                   LOWER(cm.subject) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:dateFrom IS NULL OR cm.createdAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR cm.createdAt <= :dateTo) AND " +
           "(:label IS NULL OR :label MEMBER OF cm.labels)")
    Page<ContactMessage> findWithFilters(
        @Param("status") MessageStatus status,
        @Param("query") String query,
        @Param("dateFrom") LocalDateTime dateFrom,
        @Param("dateTo") LocalDateTime dateTo,
        @Param("label") String label,
        Pageable pageable
    );
    
    @Query("SELECT COUNT(cm) FROM ContactMessage cm WHERE cm.ipHash = :ipHash AND cm.createdAt >= :since")
    int countByIpHashSince(@Param("ipHash") String ipHash, @Param("since") LocalDateTime since);
    
    @Query("SELECT cm FROM ContactMessage cm WHERE cm.status = :status ORDER BY cm.createdAt DESC")
    List<ContactMessage> findByStatusOrderByCreatedAtDesc(@Param("status") MessageStatus status);
    
    @Query("SELECT cm FROM ContactMessage cm WHERE cm.email = :email ORDER BY cm.createdAt DESC")
    List<ContactMessage> findByEmailOrderByCreatedAtDesc(@Param("email") String email);
}