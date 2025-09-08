package com.portfolio.core.application.usecase;

import com.portfolio.core.domain.admin.ResetAudit;

import java.util.List;

public interface FactoryResetUseCase {
    
    /**
     * Start a factory reset operation
     * @param startedBy User who initiated the reset
     * @param ipAddress IP address of the requester
     * @return Reset audit with job ID
     * @throws IllegalStateException if another reset is already in progress
     */
    ResetAudit startFactoryReset(String startedBy, String ipAddress);
    
    /**
     * Get active (in-progress) reset jobs
     * @return List of active reset jobs
     */
    List<ResetAudit> getActiveJobs();
    
    /**
     * Get reset audit by job ID
     * @param jobId Job ID to search for
     * @return Reset audit if found
     */
    ResetAudit getResetAuditByJobId(String jobId);
    
    /**
     * Get recent reset audit history
     * @param limit Maximum number of records to return
     * @return List of recent reset audits
     */
    List<ResetAudit> getResetHistory(int limit);
}