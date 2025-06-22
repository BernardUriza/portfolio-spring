package com.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.dto.ProjectDTO;
import com.portfolio.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllProjects() throws Exception {
        Mockito.when(projectService.getAllProjects()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testCreateProject() throws Exception {
        ProjectDTO dto = ProjectDTO.builder()
                .title("Project X")
                .description("Description")
                .link("http://link.com")
                .githubRepo("user/sparkfox")
                .createdDate(LocalDate.now())
                .stack("Angular, Spring Boot")
                .build();
        ProjectDTO saved = ProjectDTO.builder()
                .id(1L)
                .title("Project X")
                .description("Description")
                .link("http://link.com")
                .githubRepo("user/sparkfox")
                .createdDate(LocalDate.now())
                .stack("Angular, Spring Boot")
                .build();

        Mockito.when(projectService.createProject(any())).thenReturn(saved);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.stack").value("Angular, Spring Boot"));
    }


    @Test
    void testUpdateProject() throws Exception {
        ProjectDTO dto = ProjectDTO.builder()
                .id(1L)
                .title("Project Y")
                .description("New Desc")
                .link("http://link.com")
                .githubRepo("user/sparkfox")
                .createdDate(LocalDate.now())
                .stack("Angular, Spring Boot")
                .build();

        Mockito.when(projectService.updateProject(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Project Y"))
                .andExpect(jsonPath("$.stack").value("Angular, Spring Boot"));
    }
    @Test
    void testDeleteProject() throws Exception {
        Mockito.doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().is(204));
    }
}
