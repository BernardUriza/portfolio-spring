package com.portfolio.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private String message;
    private ContextInfo context;
    private AgentType agent;
}
