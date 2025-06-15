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

        mockMvc.perform(get("/api/project"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testCreateProject() throws Exception {
        ProjectDTO dto = new ProjectDTO(null, "Project X", "Description", "http://link.com", LocalDate.now().toString());
        ProjectDTO saved = new ProjectDTO(1L, "Project X", "Description", "http://link.com", LocalDate.now().toString());

        Mockito.when(projectService.createProject(any())).thenReturn(saved);

        mockMvc.perform(post("/api/project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testUpdateProject() throws Exception {
        ProjectDTO dto = new ProjectDTO(1L, "Project Y", "New Desc", "http://link.com", LocalDate.now().toString());

        Mockito.when(projectService.updateProject(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/project/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Project Y"));
    }

    @Test
    void testDeleteProject() throws Exception {
        Mockito.doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/project/1"))
                .andExpect(status().is(204));
        ;
    }
}
