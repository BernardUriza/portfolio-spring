package com.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.dto.ExperienceDTO;
import com.portfolio.service.ExperienceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExperienceController.class)
public class ExperienceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExperienceService experienceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllExperiences() throws Exception {
        Mockito.when(experienceService.getAllExperiences(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/experience")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    public void testCreateExperience() throws Exception {
        ExperienceDTO dto = new ExperienceDTO(null, "Developer", "Company", "Did stuff");
        ExperienceDTO savedDto = new ExperienceDTO(1L, "Developer", "Company", "Did stuff");

        Mockito.when(experienceService.createExperience(any(ExperienceDTO.class))).thenReturn(savedDto);

        mockMvc.perform(post("/api/experience")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Developer"));
    }

    @Test
    public void testUpdateExperience() throws Exception {
        ExperienceDTO dto = new ExperienceDTO(1L, "Senior Developer", "Company", "Did more stuff");

        Mockito.when(experienceService.updateExperience(eq(1L), any(ExperienceDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/api/experience/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Senior Developer"));
    }

    @Test
    public void testDeleteExperience() throws Exception {
        Mockito.doNothing().when(experienceService).deleteExperience(1L);

        mockMvc.perform(delete("/api/experience/1"))
                .andExpect(status().is(204));
        ;
    }
}
