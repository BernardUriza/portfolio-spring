package com.portfolio.service;

import com.portfolio.model.Project;
import com.portfolio.repository.ProjectRepository;
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

    @Test
    void generateMessageUsesProjectStack() {
        Project project = Project.builder().id(1L).stack("Java").build();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(aiService.generateDynamicMessage("Java")).thenReturn("Hi");

        ProjectService service = new ProjectService(projectRepository, aiService);
        String msg = service.generateDynamicMessage(1L);

        assertThat(msg).isEqualTo("Hi");
    }
}
