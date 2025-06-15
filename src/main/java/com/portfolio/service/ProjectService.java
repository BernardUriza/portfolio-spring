package com.portfolio.service;

import com.portfolio.dto.ProjectDTO;
import com.portfolio.model.Project;
import com.portfolio.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;

    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProjectDTO createProject(ProjectDTO dto) {
        Project project = toEntity(dto);
        project = projectRepository.save(project);
        return toDto(project);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setLink(dto.getLink());
        project.setCreatedDate(LocalDate.parse(dto.getCreatedDate(), dateFormatter));
        project = projectRepository.save(project);
        return toDto(project);
    }

    private Project toEntity(ProjectDTO dto) {
        return Project.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .link(dto.getLink())
                .createdDate(LocalDate.parse(dto.getCreatedDate(), dateFormatter))
                .build();
    }

    private ProjectDTO toDto(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setLink(project.getLink());
        dto.setCreatedDate(project.getCreatedDate().format(dateFormatter));
        return dto;
    }
}
