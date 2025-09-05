package com.portfolio.service;

import com.portfolio.model.Project;
import com.portfolio.repository.ProjectRepository;
import com.portfolio.mapper.ProjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private AIService aiService;
    @Mock
    private ProjectMapper projectMapper;

    @Test
    void generateMessageUsesProjectStack() {
        Project project = Project.builder().id(1L).stack("Java").build();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(aiService.generateDynamicMessage("Java")).thenReturn("Hi");

        ProjectService service = new ProjectService(projectRepository, aiService, projectMapper);
        String msg = service.generateDynamicMessage(1L);

        assertThat(msg).isEqualTo("Hi");
    }

    @Test
    void generateSummaryIncludesProjectData() {
        Project project = Project.builder()
                .id(2L)
                .title("Demo")
                .description("Desc")
                .stack("Java")
                .build();
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(aiService.generateProjectSummary("Demo", "Desc", "Java")).thenReturn("Summary");

        ProjectService service = new ProjectService(projectRepository, aiService, projectMapper);
        String msg = service.generateSummaryMessage(2L);

        assertThat(msg).isEqualTo("Summary");
    }
}
