package com.portfolio.service;

import com.portfolio.adapter.out.persistence.jpa.StarredProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.StarredProjectJpaRepository;
import com.portfolio.adapter.out.persistence.jpa.ProjectJpaRepository;
import com.portfolio.adapter.out.persistence.jpa.ProjectJpaEntity;
import com.portfolio.dto.StarredProjectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StarredProjectService {
    
    private final StarredProjectJpaRepository starredProjectRepository;
    private final ProjectJpaRepository projectRepository;
    private final SyncMonitorService syncMonitorService;
    
    public List<StarredProjectDto> getAllStarredProjects() {
        List<StarredProjectJpaEntity> entities = starredProjectRepository.findAllOrderByUpdatedAtDesc();
        return entities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<StarredProjectDto> getStarredProject(Long id) {
        return starredProjectRepository.findById(id)
                .map(this::mapToDto);
    }
    
    @Transactional
    public boolean deleteStarredProject(Long id) {
        Optional<StarredProjectJpaEntity> starredProjectOpt = starredProjectRepository.findById(id);
        
        if (starredProjectOpt.isEmpty()) {
            return false;
        }
        
        StarredProjectJpaEntity starredProject = starredProjectOpt.get();
        String projectName = starredProject.getName();
        
        try {
            syncMonitorService.appendLog("INFO", "Deleting starred project: " + projectName + " (ID: " + id + ")");
            
            // Delete related Project entities that reference this StarredProject
            List<ProjectJpaEntity> relatedProjects = projectRepository.findAll().stream()
                    .filter(p -> id.equals(p.getSourceStarredProjectId()))
                    .collect(Collectors.toList());
            
            if (!relatedProjects.isEmpty()) {
                syncMonitorService.appendLog("INFO", 
                    String.format("Found %d related project(s) to delete for starred project: %s", 
                                  relatedProjects.size(), projectName));
                
                for (ProjectJpaEntity project : relatedProjects) {
                    projectRepository.delete(project);
                    syncMonitorService.appendLog("DEBUG", "Deleted related project: " + project.getTitle());
                }
            }
            
            // Delete the StarredProject entity
            starredProjectRepository.delete(starredProject);
            
            syncMonitorService.appendLog("INFO", 
                String.format("Successfully deleted starred project '%s' and %d related project(s)", 
                              projectName, relatedProjects.size()));
            
            return true;
            
        } catch (Exception e) {
            log.error("Error deleting starred project with ID {}: {}", id, e.getMessage(), e);
            syncMonitorService.appendLog("ERROR", 
                String.format("Failed to delete starred project '%s': %s", projectName, e.getMessage()));
            throw new RuntimeException("Failed to delete starred project: " + e.getMessage(), e);
        }
    }
    
    public List<StarredProjectDto> getStarredProjectsByStatus(StarredProjectJpaEntity.SyncStatus status) {
        List<StarredProjectJpaEntity> entities = starredProjectRepository.findBySyncStatusOrderByUpdatedAtDesc(status);
        return entities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public long countByStatus(StarredProjectJpaEntity.SyncStatus status) {
        return starredProjectRepository.countBySyncStatus(status);
    }
    
    private StarredProjectDto mapToDto(StarredProjectJpaEntity entity) {
        return StarredProjectDto.builder()
                .id(entity.getId())
                .githubId(entity.getGithubId())
                .name(entity.getName())
                .fullName(entity.getFullName())
                .description(entity.getDescription())
                .githubRepoUrl(entity.getGithubRepoUrl())
                .homepage(entity.getHomepage())
                .language(entity.getLanguage())
                .fork(entity.getFork())
                .stargazersCount(entity.getStargazersCount())
                .topics(entity.getTopics())
                .githubCreatedAt(entity.getGithubCreatedAt())
                .githubUpdatedAt(entity.getGithubUpdatedAt())
                .readmeMarkdown(entity.getReadmeMarkdown())
                .syncStatus(entity.getSyncStatus())
                .lastSyncAttempt(entity.getLastSyncAttempt())
                .syncErrorMessage(entity.getSyncErrorMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}