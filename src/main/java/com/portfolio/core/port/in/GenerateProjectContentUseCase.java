package com.portfolio.core.port.in;

public interface GenerateProjectContentUseCase {
    
    String generateProjectSummary(Long projectId);
    
    String generateDynamicMessage(Long projectId);
}