package com.portfolio.controller;

import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.PortfolioProjectJpaRepository;
import com.portfolio.config.TestContainersConfiguration;
import com.portfolio.mock.MockClaudeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PortfolioAdminController.
 *
 * Tests portfolio CRUD operations with TestContainers PostgreSQL database.
 *
 * @author Bernard Uriza Orozco
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Import({TestContainersConfiguration.class, MockClaudeService.class})
@Disabled("TODO: Fix created_date null constraint for CI/CD - see Trello card")
class PortfolioAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PortfolioProjectJpaRepository portfolioRepository;

    private PortfolioProjectJpaEntity testProject;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        portfolioRepository.deleteAll();

        // Create test project
        testProject = new PortfolioProjectJpaEntity();
        testProject.setTitle("Test Portfolio Project");
        testProject.setDescription("Integration test project for testing CRUD operations");
        testProject.setLink("https://github.com/testuser/test-project");
        testProject.setStatus(PortfolioProjectJpaEntity.ProjectStatusJpa.ACTIVE);
        testProject.setType(PortfolioProjectJpaEntity.ProjectTypeJpa.PERSONAL);
        testProject.setCompletionStatus(PortfolioProjectJpaEntity.ProjectCompletionStatusJpa.IN_PROGRESS);
        testProject.setPriority(PortfolioProjectJpaEntity.ProjectPriorityJpa.MEDIUM);
        testProject.setMainTechnologies(List.of("Java", "Spring Boot", "PostgreSQL"));

        testProject = portfolioRepository.save(testProject);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllProjects() throws Exception {
        mockMvc.perform(get("/api/admin/portfolio")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title", is("Test Portfolio Project")))
                .andExpect(jsonPath("$.data[0].description", is("Integration test project for testing CRUD operations")))
                .andExpect(jsonPath("$.data[0].status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data[0].type", is("PERSONAL")))
                .andExpect(jsonPath("$.data[0].mainTechnologies", hasSize(3)))
                .andExpect(jsonPath("$.pagination.totalElements", is(1)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetProjectById() throws Exception {
        mockMvc.perform(get("/api/admin/portfolio/{id}", testProject.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testProject.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Test Portfolio Project")))
                .andExpect(jsonPath("$.description", notNullValue()))
                .andExpect(jsonPath("$.link", is("https://github.com/testuser/test-project")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateProject() throws Exception {
        String newProjectJson = """
                {
                    "title": "New Test Project",
                    "description": "Created via integration test",
                    "link": "https://example.com/new-project",
                    "status": "PLANNED",
                    "type": "FREELANCE",
                    "completionStatus": "NOT_STARTED",
                    "priority": "HIGH",
                    "mainTechnologies": ["TypeScript", "React", "Node.js"]
                }
                """;

        mockMvc.perform(post("/api/admin/portfolio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newProjectJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New Test Project")))
                .andExpect(jsonPath("$.status", is("PLANNED")))
                .andExpect(jsonPath("$.type", is("FREELANCE")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andExpect(jsonPath("$.mainTechnologies", hasSize(3)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateProject() throws Exception {
        String updatedProjectJson = String.format("""
                {
                    "id": %d,
                    "title": "Updated Test Project",
                    "description": "Description updated via integration test",
                    "link": "https://github.com/testuser/updated-project",
                    "status": "COMPLETED",
                    "type": "PERSONAL",
                    "completionStatus": "COMPLETED",
                    "priority": "LOW",
                    "mainTechnologies": ["Java", "Spring Boot"]
                }
                """, testProject.getId());

        mockMvc.perform(put("/api/admin/portfolio/{id}", testProject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedProjectJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Test Project")))
                .andExpect(jsonPath("$.description", is("Description updated via integration test")))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.priority", is("LOW")))
                .andExpect(jsonPath("$.mainTechnologies", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteProject() throws Exception {
        mockMvc.perform(delete("/api/admin/portfolio/{id}", testProject.getId()))
                .andExpect(status().isNoContent());

        // Verify project is deleted
        mockMvc.perform(get("/api/admin/portfolio/{id}", testProject.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetProjectById_NotFound() throws Exception {
        mockMvc.perform(get("/api/admin/portfolio/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSearchProjects() throws Exception {
        // Create additional projects for search testing
        PortfolioProjectJpaEntity project2 = new PortfolioProjectJpaEntity();
        project2.setTitle("React Dashboard");
        project2.setDescription("Modern dashboard built with React");
        project2.setStatus(PortfolioProjectJpaEntity.ProjectStatusJpa.COMPLETED);
        project2.setType(PortfolioProjectJpaEntity.ProjectTypeJpa.PROFESSIONAL);
        project2.setCompletionStatus(PortfolioProjectJpaEntity.ProjectCompletionStatusJpa.LIVE);
        project2.setPriority(PortfolioProjectJpaEntity.ProjectPriorityJpa.HIGH);
        project2.setMainTechnologies(List.of("React", "TypeScript"));
        portfolioRepository.save(project2);

        mockMvc.perform(get("/api/admin/portfolio")
                        .param("search", "React")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].title", containsString("React")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testPaginationWorks() throws Exception {
        // Create multiple projects
        for (int i = 1; i <= 15; i++) {
            PortfolioProjectJpaEntity project = new PortfolioProjectJpaEntity();
            project.setTitle("Project " + i);
            project.setDescription("Test project " + i);
            project.setStatus(PortfolioProjectJpaEntity.ProjectStatusJpa.COMPLETED);
            project.setType(PortfolioProjectJpaEntity.ProjectTypeJpa.PERSONAL);
            project.setCompletionStatus(PortfolioProjectJpaEntity.ProjectCompletionStatusJpa.LIVE);
            project.setPriority(PortfolioProjectJpaEntity.ProjectPriorityJpa.MEDIUM);
            project.setMainTechnologies(List.of("Java"));
            portfolioRepository.save(project);
        }

        // Test first page
        mockMvc.perform(get("/api/admin/portfolio")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(10)))
                .andExpect(jsonPath("$.pagination.page", is(0)))
                .andExpect(jsonPath("$.pagination.size", is(10)))
                .andExpect(jsonPath("$.pagination.totalElements", is(16)))
                .andExpect(jsonPath("$.pagination.totalPages", is(2)));

        // Test second page
        mockMvc.perform(get("/api/admin/portfolio")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(6)))
                .andExpect(jsonPath("$.pagination.page", is(1)));
    }

    @Test
    void testUnauthorizedAccessDenied() throws Exception {
        // Without @WithMockUser, request should be unauthorized
        mockMvc.perform(get("/api/admin/portfolio"))
                .andExpect(status().isUnauthorized());
    }
}
