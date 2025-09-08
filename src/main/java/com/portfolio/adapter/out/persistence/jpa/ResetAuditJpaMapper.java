package com.portfolio.adapter.out.persistence.jpa;

import com.portfolio.core.domain.admin.ResetAudit;
import org.springframework.stereotype.Component;

@Component
public class ResetAuditJpaMapper {
    
    public ResetAudit toDomain(ResetAuditJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return ResetAudit.builder()
                .id(entity.getId())
                .jobId(entity.getJobId())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .startedBy(entity.getStartedBy())
                .status(entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .tablesCleared(entity.getTablesCleared())
                .durationMs(entity.getDurationMs())
                .ipAddress(entity.getIpAddress())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public ResetAuditJpaEntity toJpaEntity(ResetAudit domain) {
        if (domain == null) {
            return null;
        }
        
        return ResetAuditJpaEntity.builder()
                .id(domain.getId())
                .jobId(domain.getJobId())
                .startedAt(domain.getStartedAt())
                .finishedAt(domain.getFinishedAt())
                .startedBy(domain.getStartedBy())
                .status(domain.getStatus())
                .errorMessage(domain.getErrorMessage())
                .tablesCleared(domain.getTablesCleared())
                .durationMs(domain.getDurationMs())
                .ipAddress(domain.getIpAddress())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}