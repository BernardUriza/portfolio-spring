package com.portfolio.core.port.out;

public interface AIServicePort {
    
    String generateProjectSummary(String title, String description, String technologies);
    
    String generateDynamicMessage(String technologies);
    
    String analyzeTechnologies(java.util.List<String> technologies);
}