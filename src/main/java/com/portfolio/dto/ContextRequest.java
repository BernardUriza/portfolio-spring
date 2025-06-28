package com.portfolio.dto;

import lombok.Data;

@Data
public class ContextRequest {
    private String section;
    private String projectId;
    private String action;
    private Object userContext;
}
