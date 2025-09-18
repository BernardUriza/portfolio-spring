package com.portfolio.adapter.in.rest.dto;

public class MessageResponse {
    private String message;

    public MessageResponse() {}

    public MessageResponse(String message) { this.message = message; }

    public static Builder builder() { return new Builder(); }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public static final class Builder {
        private String message;
        public Builder message(String message) { this.message = message; return this; }
        public MessageResponse build() { return new MessageResponse(message); }
    }
}
