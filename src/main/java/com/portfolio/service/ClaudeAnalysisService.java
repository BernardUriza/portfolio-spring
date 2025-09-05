package com.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeAnalysisService {
    
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(120))
            .build();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;
    
    @Value("${anthropic.api.url:https://api.anthropic.com/v1/messages}")
    private String anthropicApiUrl;
    
    public SemanticAnalysisResult analyzeRepository(String repoName, String description, 
                                                   List<String> topics, String language, String homepage) {
        
        if (anthropicApiKey == null || anthropicApiKey.trim().isEmpty()) {
            log.warn("Anthropic API key not configured, skipping semantic analysis for: {}", repoName);
            return createEmptyResult();
        }
        
        try {
            String prompt = buildAnalysisPrompt(repoName, description, topics, language, homepage);
            String claudeResponse = callClaudeAPI(prompt);
            return parseClaudeResponse(claudeResponse, repoName);
            
        } catch (Exception e) {
            log.error("Failed to analyze repository '{}' with Claude: {}", repoName, e.getMessage(), e);
            return createEmptyResult();
        }
    }
    
    private String buildAnalysisPrompt(String repoName, String description, 
                                     List<String> topics, String language, String homepage) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Given this GitHub repository metadata, infer the following in JSON format:\n\n");
        prompt.append("- skills: array of relevant technical skills/technologies used\n");
        prompt.append("- experiences: array of development experience descriptions gained from this project\n");
        prompt.append("- project: object with name, description, estimatedDurationWeeks, mainTechnologies, githubUrl\n\n");
        prompt.append("Repository Metadata:\n");
        prompt.append("---\n");
        prompt.append("Repo Name: ").append(repoName != null ? repoName : "N/A").append("\n");
        prompt.append("Description: ").append(description != null ? description : "No description provided").append("\n");
        prompt.append("Topics: ").append(topics != null && !topics.isEmpty() ? String.join(", ", topics) : "None").append("\n");
        prompt.append("Language: ").append(language != null ? language : "Unknown").append("\n");
        prompt.append("Homepage: ").append(homepage != null ? homepage : "N/A").append("\n");
        prompt.append("---\n\n");
        prompt.append("Return ONLY valid JSON in this exact format:\n");
        prompt.append("{\n");
        prompt.append("  \"skills\": [\"skill1\", \"skill2\"],\n");
        prompt.append("  \"experiences\": [\"experience1\", \"experience2\"],\n");
        prompt.append("  \"project\": {\n");
        prompt.append("    \"name\": \"Project Name\",\n");
        prompt.append("    \"description\": \"Brief project description\",\n");
        prompt.append("    \"estimatedDurationWeeks\": 4,\n");
        prompt.append("    \"mainTechnologies\": [\"tech1\", \"tech2\"],\n");
        prompt.append("    \"githubUrl\": \"repo_url_here\"\n");
        prompt.append("  }\n");
        prompt.append("}");
        
        return prompt.toString();
    }
    
    private String callClaudeAPI(String prompt) throws IOException {
        String requestBody = objectMapper.writeValueAsString(new ClaudeRequest(prompt));
        
        RequestBody body = RequestBody.create(requestBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(anthropicApiUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-api-key", anthropicApiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("Claude API call failed with status {}: {}", response.code(), errorBody);
                throw new IOException("Claude API call failed: " + response.code() + " " + errorBody);
            }
            
            String responseBody = response.body().string();
            log.debug("Claude API response: {}", responseBody);
            
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode contentArray = responseNode.get("content");
            if (contentArray != null && contentArray.isArray() && contentArray.size() > 0) {
                JsonNode firstContent = contentArray.get(0);
                JsonNode textNode = firstContent.get("text");
                if (textNode != null) {
                    return textNode.asText();
                }
            }
            
            throw new IOException("Unexpected Claude API response format");
        }
    }
    
    private SemanticAnalysisResult parseClaudeResponse(String claudeResponse, String repoName) {
        try {
            JsonNode jsonResponse = objectMapper.readTree(claudeResponse);
            
            List<String> skills = objectMapper.convertValue(
                jsonResponse.get("skills"), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            
            List<String> experiences = objectMapper.convertValue(
                jsonResponse.get("experiences"), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            
            JsonNode projectNode = jsonResponse.get("project");
            ProjectInfo projectInfo = null;
            if (projectNode != null) {
                projectInfo = objectMapper.convertValue(projectNode, ProjectInfo.class);
            }
            
            log.info("Successfully parsed Claude analysis for repository: {}", repoName);
            return new SemanticAnalysisResult(skills, experiences, projectInfo);
            
        } catch (Exception e) {
            log.error("Failed to parse Claude response for repository '{}': {}", repoName, e.getMessage());
            log.debug("Claude response was: {}", claudeResponse);
            return createEmptyResult();
        }
    }
    
    private SemanticAnalysisResult createEmptyResult() {
        return new SemanticAnalysisResult(List.of(), List.of(), null);
    }
    
    // Inner classes for request/response
    private record ClaudeRequest(String model, int max_tokens, Message[] messages) {
        ClaudeRequest(String prompt) {
            this("claude-3-5-sonnet-20241022", 2000, new Message[]{new Message("user", prompt)});
        }
        
        private record Message(String role, String content) {}
    }
    
    public record SemanticAnalysisResult(
        List<String> skills,
        List<String> experiences,
        ProjectInfo project
    ) {}
    
    public record ProjectInfo(
        String name,
        String description,
        Integer estimatedDurationWeeks,
        List<String> mainTechnologies,
        String githubUrl
    ) {}
}