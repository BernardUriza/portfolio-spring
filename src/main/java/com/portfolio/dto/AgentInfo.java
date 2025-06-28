package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentInfo {
    private AgentType type;
    private String name;
    private String icon;
    private String description;
}
