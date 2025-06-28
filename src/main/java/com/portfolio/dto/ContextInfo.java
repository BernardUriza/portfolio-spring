package com.portfolio.dto;

import lombok.Data;

@Data
public class ContextInfo {
    private String section;
    private String projectId;
    private Object metadata;
    private String timestamp;
}
