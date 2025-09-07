package com.portfolio.adapter.out.external.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.core.port.out.AIServicePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AIServiceImpl {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String anthropicApiKey;
    private final String anthropicApiUrl;
    
    public AIServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper,
                        @Value("${anthropic.api.key:}") String anthropicApiKey,
                        @Value("${anthropic.api.url:https://api.anthropic.com/v1/messages}") String anthropicApiUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.anthropicApiKey = anthropicApiKey;
        this.anthropicApiUrl = anthropicApiUrl;
        
        if (anthropicApiKey != null && !anthropicApiKey.trim().isEmpty()) {
            log.info("Anthropic API key configured successfully");
        } else {
            log.warn("Anthropic API key not configured - will use mock data");
        }
    }
    
    public String generateProjectSummary(String title, String description, String technologies) {
        log.info("Generating project summary for: {}", title);
        
        // TODO: Implement actual AI service call (Claude)
        // For now, return a mock response
        return String.format("AI Summary for '%s': This project uses %s and focuses on %s", 
                title, technologies, description.substring(0, Math.min(description.length(), 50)));
    }
    
    public String generateDynamicMessage(String technologies) {
        log.info("Generating dynamic message for technologies: {}", technologies);
        
        // TODO: Implement actual AI service call
        return String.format("Dynamic message: Great choice using %s! These technologies are trending.", 
                technologies != null ? technologies : "modern tech stack");
    }
    
    public AIServicePort.ClaudeAnalysisResult analyzeRepository(String repoName, String description, 
                                                               String readmeContent, List<String> topics, String language) {
        log.info("Analyzing repository: {} with Claude API", repoName);
        
        if (anthropicApiKey == null || anthropicApiKey.trim().isEmpty()) {
            log.warn("Claude API key not configured, returning mock data for repository: {}", repoName);
            return createMockAnalysisResult(repoName, description, topics, language);
        }
        
        try {
            log.info("Making actual Claude API call for repository: {}", repoName);
            String prompt = buildAnalysisPrompt(repoName, description, readmeContent, topics, language);
            String response = callClaudeApi(prompt);
            log.info("Claude API response received successfully for: {}", repoName);
            return parseClaudeResponse(response, repoName, description, topics);
        } catch (Exception e) {
            log.error("Error calling Claude API for repository: {}, falling back to mock data", repoName, e);
            return createMockAnalysisResult(repoName, description, topics, language);
        }
    }
    
    private String buildAnalysisPrompt(String repoName, String description, String readmeContent, 
                                      List<String> topics, String language) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this GitHub repository and extract structured information:\n\n");
        prompt.append("Repository Name: ").append(repoName).append("\n");
        prompt.append("Description: ").append(description != null ? description : "No description").append("\n");
        prompt.append("Language: ").append(language != null ? language : "Unknown").append("\n");
        prompt.append("Topics: ").append(topics != null ? String.join(", ", topics) : "None").append("\n");
        
        if (readmeContent != null && !readmeContent.trim().isEmpty()) {
            prompt.append("README Content:\n").append(readmeContent.substring(0, Math.min(readmeContent.length(), 4000))).append("\n");
        }
        
        prompt.append("\nExtract and return ONLY a valid JSON object with this exact structure:\n");
        prompt.append("{\n");
        prompt.append("  \"project\": {\n");
        prompt.append("    \"name\": \"project name\",\n");
        prompt.append("    \"description\": \"concise project description (max 500 chars)\",\n");
        prompt.append("    \"estimatedDurationWeeks\": number_or_null,\n");
        prompt.append("    \"technologies\": [\"tech1\", \"tech2\"],\n");
        prompt.append("    \"url\": \"github_url_or_homepage\"\n");
        prompt.append("  },\n");
        prompt.append("  \"skills\": [\"skill1\", \"skill2\", \"skill3\"],\n");
        prompt.append("  \"experiences\": [\"experience1\", \"experience2\"]\n");
        prompt.append("}\n\n");
        prompt.append("Return ONLY the JSON, no additional text.");
        
        return prompt.toString();
    }
    
    private String callClaudeApi(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicApiKey);
        headers.set("anthropic-version", "2023-06-01");
        
        Map<String, Object> requestBody = Map.of(
            "model", "claude-3-haiku-20240307",
            "max_tokens", 1000,
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            )
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            anthropicApiUrl, HttpMethod.POST, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode responseNode = objectMapper.readTree(response.getBody());
            JsonNode contentArray = responseNode.path("content");
            if (contentArray.isArray() && contentArray.size() > 0) {
                return contentArray.get(0).path("text").asText();
            }
        }
        
        throw new RuntimeException("Failed to get valid response from Claude API");
    }
    
    private AIServicePort.ClaudeAnalysisResult parseClaudeResponse(String response, String fallbackName, 
                                                                  String fallbackDescription, List<String> fallbackTopics) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            
            // Parse project data
            JsonNode projectNode = jsonNode.path("project");
            AIServicePort.ProjectData projectData = new AIServicePort.ProjectData(
                projectNode.path("name").asText(fallbackName),
                projectNode.path("description").asText(fallbackDescription),
                projectNode.path("estimatedDurationWeeks").isNull() ? null : projectNode.path("estimatedDurationWeeks").asInt(),
                parseStringArray(projectNode.path("technologies"), fallbackTopics),
                projectNode.path("url").asText("")
            );
            
            // Parse skills and experiences
            List<String> skills = parseStringArray(jsonNode.path("skills"), new ArrayList<>());
            List<String> experiences = parseStringArray(jsonNode.path("experiences"), new ArrayList<>());
            
            return new AIServicePort.ClaudeAnalysisResult(projectData, skills, experiences);
            
        } catch (Exception e) {
            log.error("Error parsing Claude response, using fallback data", e);
            return createMockAnalysisResult(fallbackName, fallbackDescription, fallbackTopics, null);
        }
    }
    
    private List<String> parseStringArray(JsonNode arrayNode, List<String> fallback) {
        if (!arrayNode.isArray()) return fallback != null ? new ArrayList<>(fallback) : new ArrayList<>();
        
        List<String> result = new ArrayList<>();
        arrayNode.forEach(node -> result.add(node.asText()));
        return result;
    }
    
    private AIServicePort.ClaudeAnalysisResult createMockAnalysisResult(String repoName, String description, 
                                                                       List<String> topics, String language) {
        List<String> technologies = topics != null ? new ArrayList<>(topics) : new ArrayList<>();
        if (language != null && !technologies.contains(language)) {
            technologies.add(language);
        }
        
        AIServicePort.ProjectData projectData = new AIServicePort.ProjectData(
            repoName,
            description != null ? description : "GitHub repository: " + repoName,
            null,
            technologies,
            ""
        );
        
        List<String> skills = new ArrayList<>(technologies);
        if (language != null && !skills.contains(language)) {
            skills.add(language);
        }
        
        List<String> experiences = Arrays.asList("Software Development", "Open Source Development");
        
        return new AIServicePort.ClaudeAnalysisResult(projectData, skills, experiences);
    }
}