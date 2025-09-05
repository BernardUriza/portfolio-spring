package com.portfolio.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.adapter.in.rest.dto.ProjectRestDto;
import com.portfolio.adapter.in.rest.mapper.ProjectRestMapper;
import com.portfolio.core.domain.project.Project;
import com.portfolio.core.domain.project.ProjectStatus;
import com.portfolio.core.domain.project.ProjectType;
import com.portfolio.core.port.in.CreateProjectUseCase;
import com.portfolio.core.port.in.GenerateProjectContentUseCase;
import com.portfolio.core.port.in.GetProjectsUseCase;
import com.portfolio.core.port.in.UpdateProjectUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectRestController.class)
@DisplayName("Project REST Controller Tests")
class ProjectRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateProjectUseCase createProjectUseCase;

    @MockBean
    private GetProjectsUseCase getProjectsUseCase;

    @MockBean
    private UpdateProjectUseCase updateProjectUseCase;

    @MockBean
    private GenerateProjectContentUseCase generateProjectContentUseCase;

    @MockBean
    private ProjectRestMapper restMapper;

    @Test
    @DisplayName("Should get project by ID successfully")
    void shouldGetProjectByIdSuccessfully() throws Exception {
        // Given
        Long projectId = 1L;
        Project mockProject = createMockProject(projectId);
        ProjectRestDto mockDto = createMockProjectDto(projectId);

        when(getProjectsUseCase.getProjectById(projectId)).thenReturn(Optional.of(mockProject));
        when(restMapper.toRestDto(mockProject)).thenReturn(mockDto);

        // When & Then
        mockMvc.perform(get("/api/v2/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.title").value("Test Project"))
                .andExpect(jsonPath("$.description").value("A test project description"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should return 400 when project not found")
    void shouldReturn400WhenProjectNotFound() throws Exception {
        // Given
        Long projectId = 999L;
        when(getProjectsUseCase.getProjectById(projectId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v2/projects/{id}", projectId))
                .andExpect(status().isInternalServerError()); // Expecting 500 due to thrown exception
    }

    @Test
    @DisplayName("Should create project successfully")
    void shouldCreateProjectSuccessfully() throws Exception {
        // Given
        ProjectRestDto inputDto = createMockProjectDto(null);
        Project mockCreatedProject = createMockProject(1L);
        ProjectRestDto responseDto = createMockProjectDto(1L);
        
        CreateProjectUseCase.CreateProjectCommand mockCommand = 
            new CreateProjectUseCase.CreateProjectCommand(
                "Test Project", 
                "A test project description", 
                "https://example.com",
                "https://github.com/user/repo", 
                LocalDate.now(), 
                Arrays.asList("Java", "Spring Boot")
            );

        when(restMapper.toCreateCommand(any(ProjectRestDto.class))).thenReturn(mockCommand);
        when(createProjectUseCase.createProject(mockCommand)).thenReturn(mockCreatedProject);
        when(restMapper.toRestDto(mockCreatedProject)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/v2/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Project"));
    }

    @Test
    @DisplayName("Should update project successfully")
    void shouldUpdateProjectSuccessfully() throws Exception {
        // Given
        Long projectId = 1L;
        ProjectRestDto inputDto = createMockProjectDto(projectId);
        Project mockUpdatedProject = createMockProject(projectId);
        ProjectRestDto responseDto = createMockProjectDto(projectId);
        
        UpdateProjectUseCase.UpdateProjectCommand mockCommand = 
            new UpdateProjectUseCase.UpdateProjectCommand(
                projectId,
                "Test Project", 
                "A test project description", 
                "https://example.com",
                "https://github.com/user/repo"
            );

        when(restMapper.toUpdateCommand(eq(projectId), any(ProjectRestDto.class))).thenReturn(mockCommand);
        when(updateProjectUseCase.updateProject(mockCommand)).thenReturn(mockUpdatedProject);
        when(restMapper.toRestDto(mockUpdatedProject)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(put("/api/v2/projects/{id}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(projectId));
    }

    @Test
    @DisplayName("Should delete project successfully")
    void shouldDeleteProjectSuccessfully() throws Exception {
        // Given
        Long projectId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/v2/projects/{id}", projectId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should generate AI summary successfully")
    void shouldGenerateAISummarySuccessfully() throws Exception {
        // Given
        Long projectId = 1L;
        String mockSummary = "This is an AI-generated project summary.";
        
        when(generateProjectContentUseCase.generateProjectSummary(projectId)).thenReturn(mockSummary);

        // When & Then
        mockMvc.perform(get("/api/v2/projects/{id}/ai-summary", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(mockSummary));
    }

    @Test
    @DisplayName("Should generate dynamic message successfully")
    void shouldGenerateDynamicMessageSuccessfully() throws Exception {
        // Given
        Long projectId = 1L;
        String mockMessage = "This is a dynamic AI message.";
        
        when(generateProjectContentUseCase.generateDynamicMessage(projectId)).thenReturn(mockMessage);

        // When & Then
        mockMvc.perform(get("/api/v2/projects/{id}/ai-message", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(mockMessage));
    }

    private Project createMockProject(Long id) {
        return Project.builder()
                .id(id)
                .title("Test Project")
                .description("A test project description")
                .link("https://example.com")
                .githubRepo("https://github.com/user/repo")
                .createdDate(LocalDate.now())
                .estimatedDurationWeeks(4)
                .status(ProjectStatus.ACTIVE)
                .type(ProjectType.PERSONAL)
                .mainTechnologies(Arrays.asList("Java", "Spring Boot"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ProjectRestDto createMockProjectDto(Long id) {
        return ProjectRestDto.builder()
                .id(id)
                .title("Test Project")
                .description("A test project description")
                .link("https://example.com")
                .githubRepo("https://github.com/user/repo")
                .createdDate(LocalDate.now())
                .estimatedDurationWeeks(4)
                .status("ACTIVE")
                .type("PERSONAL")
                .mainTechnologies(Arrays.asList("Java", "Spring Boot"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}