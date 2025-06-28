package com.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.dto.*;
import com.portfolio.service.AIService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testUpdateContext() throws Exception {
        ContextRequest req = new ContextRequest();
        req.setSection("projects");
        req.setProjectId("1");
        req.setAction("open");
        req.setUserContext(Map.of());

        Mockito.doNothing().when(aiService).recordContext(Mockito.eq("default"), Mockito.any());

        mockMvc.perform(post("/api/chat/context")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAgent() throws Exception {
        AgentInfo info = new AgentInfo(AgentType.PROJECT_GUIDE, "Project Guide", "i", "d");
        Mockito.when(aiService.getAgentInfo(AgentType.PROJECT_GUIDE)).thenReturn(info);

        mockMvc.perform(get("/api/chat/agent/PROJECT_GUIDE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Project Guide"));
    }

    @Test
    void testSendMessage() throws Exception {
        ChatMessageRequest req = new ChatMessageRequest();
        ContextInfo ctx = new ContextInfo();
        ctx.setSection("projects");
        req.setMessage("Hi");
        req.setContext(ctx);
        req.setAgent(AgentType.PROJECT_GUIDE);

        Mockito.when(aiService.generateContextAwareResponse("Hi", "default", ctx, AgentType.PROJECT_GUIDE))
                .thenReturn("Answer");

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Answer"));
    }
}
