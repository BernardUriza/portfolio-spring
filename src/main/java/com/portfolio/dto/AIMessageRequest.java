package com.portfolio.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIMessageRequest {
    private List<String> stack;
    private String prompt;
}
