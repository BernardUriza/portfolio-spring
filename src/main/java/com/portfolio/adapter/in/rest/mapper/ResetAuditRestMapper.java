package com.portfolio.adapter.in.rest.mapper;

import com.portfolio.adapter.in.rest.dto.ResetAuditDto;
import com.portfolio.core.domain.admin.ResetAudit;
import org.springframework.stereotype.Component;

@Component
public class ResetAuditRestMapper {
    
    public ResetAuditDto toRestDto(ResetAudit domain) {
        if (domain == null) {
            return null;
        }
        
        return ResetAuditDto.builder()
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