package com.portfolio.application.usecase;

import com.portfolio.core.domain.project.Project;
import com.portfolio.core.port.in.GenerateProjectContentUseCase;
import com.portfolio.core.port.out.AIServicePort;
import com.portfolio.core.port.out.ProjectRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateProjectContentUseCaseImpl implements GenerateProjectContentUseCase {
    
    private final ProjectRepositoryPort projectRepository;
    private final AIServicePort aiService;
    
    @Override
    @Transactional(readOnly = true)
    public String generateProjectSummary(Long projectId) {
        log.info("Generating AI summary for project ID: {}", projectId);
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        
        String technologies = String.join(", ", project.getMainTechnologies());
        String summary = aiService.generateProjectSummary(
                project.getTitle(),
                project.getDescription(),
                technologies
        );
        
        log.info("AI summary generated successfully for project ID: {}", projectId);
        return summary;
    }
    
    @Override
    @Transactional(readOnly = true)
    public String generateDynamicMessage(Long projectId) {
        log.info("Generating dynamic message for project ID: {}", projectId);
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        
        String technologies = String.join(", ", project.getMainTechnologies());
        String message = aiService.generateDynamicMessage(technologies);
        
        log.info("Dynamic message generated successfully for project ID: {}", projectId);
        return message;
    }
}