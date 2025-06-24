package com.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.dto.StackRequest;
import com.portfolio.service.AIService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AIController.class)
class AIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetMessage() throws Exception {
        StackRequest req = new StackRequest();
        req.setStack(List.of("Angular", "Spring Boot"));

        Mockito.when(aiService.generateDynamicMessage("Angular, Spring Boot"))
                .thenReturn("Answer");

        mockMvc.perform(post("/api/ai/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("Answer"));
    }
}
