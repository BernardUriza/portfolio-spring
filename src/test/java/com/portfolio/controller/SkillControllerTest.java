package com.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.dto.SkillDTO;
import com.portfolio.service.SkillService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SkillController.class)
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SkillService skillService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllSkills() throws Exception {
        Mockito.when(skillService.getAllSkills()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/skill"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testCreateSkill() throws Exception {
        SkillDTO dto = new SkillDTO(null, "Java", "Programming language");
        SkillDTO saved = new SkillDTO(1L, "Java", "Programming language");

        Mockito.when(skillService.createSkill(any())).thenReturn(saved);

        mockMvc.perform(post("/api/skill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testUpdateSkill() throws Exception {
        SkillDTO dto = new SkillDTO(1L, "Java", "Updated desc");

        Mockito.when(skillService.updateSkill(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/skill/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated desc"));
    }

    @Test
    void testDeleteSkill() throws Exception {
        Mockito.doNothing().when(skillService).deleteSkill(1L);

        mockMvc.perform(delete("/api/skill/1"))
                .andExpect(status().isOk());
    }
}
