package com.portfolio.service;

import com.portfolio.dto.ProjectDTO;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.model.Project;
import com.portfolio.mapper.ProjectMapper;
import com.portfolio.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AIService aiService;
    private final ProjectMapper projectMapper;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;

    public Page<ProjectDTO> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(projectMapper::toDto);
    }

    @Transactional
    public ProjectDTO createProject(ProjectDTO dto) {
        Project project = projectMapper.toEntity(dto);
        project = projectRepository.save(project);
        return projectMapper.toDto(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setLink(dto.getLink());
        project.setGithubRepo(dto.getGithubRepo());
        project.setCreatedDate(dto.getCreatedDate());
        project = projectRepository.save(project);
        return projectMapper.toDto(project);
    }

    public ProjectDTO getProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        return projectMapper.toDto(project);
    }

    public String generateDynamicMessage(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return aiService.generateDynamicMessage(String.join(", ", project.getMainTechnologies()));
    }

    public String generateSummaryMessage(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return aiService.generateProjectSummary(project.getTitle(), project.getDescription(), String.join(", ", project.getMainTechnologies()));
    }

}
