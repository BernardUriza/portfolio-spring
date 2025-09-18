package com.portfolio.adapter.in.rest.dto;

public class FactoryResetResponseDto {
    private String jobId;
    private String message;
    private String streamUrl;

    public FactoryResetResponseDto() {}

    public FactoryResetResponseDto(String jobId, String message, String streamUrl) {
        this.jobId = jobId;
        this.message = message;
        this.streamUrl = streamUrl;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }

    public static Builder builder() { return new Builder(); }
    public static final class Builder {
        private String jobId;
        private String message;
        private String streamUrl;
        public Builder jobId(String jobId) { this.jobId = jobId; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder streamUrl(String streamUrl) { this.streamUrl = streamUrl; return this; }
        public FactoryResetResponseDto build() { return new FactoryResetResponseDto(jobId, message, streamUrl); }
    }
}
