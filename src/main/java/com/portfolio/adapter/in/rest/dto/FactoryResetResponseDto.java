package com.portfolio.adapter.in.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FactoryResetResponseDto {
    private final String jobId;
    private final String message;
    private final String streamUrl;
}