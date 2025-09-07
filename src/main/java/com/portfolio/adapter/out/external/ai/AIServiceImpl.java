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
    private String portfolioToneContext = null;
    
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
        
        loadPortfolioToneContext();
    }
    
    private void loadPortfolioToneContext() {
        try {
            // Try to load the portfolio landing page content
            String frontendPath = System.getProperty("user.dir");
            if (frontendPath.contains("portfolio-backend")) {
                frontendPath = frontendPath.replace("portfolio-backend", "portfolio-frontend");
            }
            
            java.nio.file.Path indexPath = java.nio.file.Paths.get(frontendPath, "src", "index.html");
            if (java.nio.file.Files.exists(indexPath)) {
                String indexContent = new String(java.nio.file.Files.readAllBytes(indexPath), java.nio.charset.StandardCharsets.UTF_8);
                
                // Extract key portfolio tone elements
                StringBuilder toneBuilder = new StringBuilder();
                toneBuilder.append("Portfolio Landing Page Context:\n");
                
                if (indexContent.contains("Catalytic Architect")) {
                    toneBuilder.append("- Professional title: Catalytic Architect & Full-Stack Engineer\n");
                }
                if (indexContent.contains("technical transformation")) {
                    toneBuilder.append("- Mission: Architect of technical transformation. Design and execute software systems that catalyze change.\n");
                }
                
                // Load i18n content if available
                java.nio.file.Path i18nPath = java.nio.file.Paths.get(frontendPath, "src", "app", "core", "i18n.service.ts");
                if (java.nio.file.Files.exists(i18nPath)) {
                    String i18nContent = new String(java.nio.file.Files.readAllBytes(i18nPath), java.nio.charset.StandardCharsets.UTF_8);
                    
                    // Extract key messages
                    if (i18nContent.contains("Your team doesn't need more developers")) {
                        toneBuilder.append("- Core message: Your team doesn't need more developers. It needs a phase catalyst.\n");
                    }
                    if (i18nContent.contains("I break systems")) {
                        toneBuilder.append("- Philosophy: I break systems that have outgrown their chaos but are not yet ready for stability.\n");
                    }
                    if (i18nContent.contains("Technical catalyst")) {
                        toneBuilder.append("- Identity: Technical catalyst and architecture strategist. I expose what is broken and engineer coherence where chaos once reigned.\n");
                    }
                }
                
                portfolioToneContext = toneBuilder.toString();
                log.info("Portfolio tone context loaded successfully");
            } else {
                log.warn("Portfolio frontend files not found, using default tone context");
                portfolioToneContext = getDefaultPortfolioTone();
            }
        } catch (Exception e) {
            log.warn("Error loading portfolio tone context, using defaults: " + e.getMessage());
            portfolioToneContext = getDefaultPortfolioTone();
        }
    }
    
    private String getDefaultPortfolioTone() {
        return "Portfolio Tone Context:\n" +
               "- Title: \"Catalytic Architect & Full-Stack Engineer\"\n" +
               "- Hero: \"Your team doesn't need more developers. It needs a phase catalyst.\"\n" +
               "- Sub: \"I break systems that have outgrown their chaos but are not yet ready for stability.\"\n" +
               "- About: \"Technical catalyst and architecture strategist. I expose what is broken and engineer coherence where chaos once reigned. I do not adapt, I transform. I do not decorate, I reconfigure.\"\n" +
               "- Key phrases: \"Dissonance sparks transformation\", \"Refactoring cultures drives true development\", \"Code is the output, not the objective\"\n";
    }
    
    public String generateProjectSummary(String title, String description, String technologies) {
        if (title == null || title.trim().isEmpty()) {
            log.warn("Invalid title provided for project summary generation");
            return "No project summary available - invalid title";
        }
        
        log.info("Generating project summary for: {}", title);
        
        String safeDescription = description != null ? description : "No description available";
        String safeTechnologies = technologies != null ? technologies : "No technologies specified";
        
        if (safeDescription.length() > 50) {
            safeDescription = safeDescription.substring(0, 50) + "...";
        }
        
        return String.format("AI Summary for '%s': This project uses %s and focuses on %s", 
                title, safeTechnologies, safeDescription);
    }
    
    public String generateDynamicMessage(String technologies) {
        log.info("Generating dynamic message for technologies: {}", technologies);
        
        // TODO: Implement actual AI service call
        return String.format("Dynamic message: Great choice using %s! These technologies are trending.", 
                technologies != null ? technologies : "modern tech stack");
    }
    
    public AIServicePort.ClaudeAnalysisResult analyzeRepository(String repoName, String description, 
                                                               String readmeContent, List<String> topics, String language) {
        if (repoName == null || repoName.trim().isEmpty()) {
            log.warn("Invalid repository name provided for analysis");
            return createMockAnalysisResult("unknown-repo", description, topics, language);
        }
        
        log.info("Analyzing repository: {} with Claude API", repoName);
        
        if (anthropicApiKey == null || anthropicApiKey.trim().isEmpty()) {
            log.warn("Claude API key not configured, returning mock data for repository: {}", repoName);
            return createMockAnalysisResult(repoName, description, topics, language);
        }
        
        try {
            log.debug("Making Claude API call for repository: {}", repoName);
            String prompt = buildAnalysisPrompt(repoName, description, readmeContent, topics, language);
            String response = callClaudeApi(prompt);
            log.debug("Claude API response received successfully for: {}", repoName);
            return parseClaudeResponse(response, repoName, description, topics);
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for Claude API call for repository: {}", repoName, e);
            return createMockAnalysisResult(repoName, description, topics, language);
        } catch (Exception e) {
            log.error("Error calling Claude API for repository: {}, falling back to mock data. Error: {}", 
                     repoName, e.getMessage());
            return createMockAnalysisResult(repoName, description, topics, language);
        }
    }
    
    private String buildAnalysisPrompt(String repoName, String description, String readmeContent, 
                                      List<String> topics, String language) {
        if (repoName == null || repoName.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository name cannot be null or empty");
        }
        
        StringBuilder prompt = new StringBuilder(4096);
        
        // Add dynamically loaded portfolio tone context
        prompt.append("You are creating content for Bernard Uriza's portfolio website.\n\n");
        prompt.append(portfolioToneContext != null ? portfolioToneContext : getDefaultPortfolioTone());
        prompt.append("\n");
        
        prompt.append("Analyze this GitHub repository and create content that matches the portfolio's bold, transformative tone:\n\n");
        prompt.append("Repository Name: ").append(repoName.trim()).append("\n");
        prompt.append("Original GitHub Description: ").append(description != null && !description.trim().isEmpty() ? 
                     description.trim() : "No description available").append("\n");
        prompt.append("Language: ").append(language != null && !language.trim().isEmpty() ? 
                     language.trim() : "Unknown").append("\n");
        prompt.append("Topics: ").append(topics != null && !topics.isEmpty() ? 
                     String.join(", ", topics) : "None").append("\n");
        
        if (readmeContent != null && !readmeContent.trim().isEmpty()) {
            int maxReadmeLength = 2500; // Reduced to leave room for other content
            String truncatedReadme = readmeContent.length() > maxReadmeLength ? 
                                   readmeContent.substring(0, maxReadmeLength) + "... [truncated]" : 
                                   readmeContent;
            prompt.append("README Content:\n").append(truncatedReadme).append("\n");
        }
        
        prompt.append("\nCreate a JSON response where:\n");
        prompt.append("1. The project description is REWRITTEN in the portfolio's bold, transformative tone (not just copying the GitHub description)\n");
        prompt.append("2. Focus on the transformative impact and architectural significance\n");
        prompt.append("3. Use powerful, decisive language that matches the portfolio style\n");
        prompt.append("4. Skills should be technical and strategic\n");
        prompt.append("5. Experiences should reflect leadership and transformation\n\n");
        
        prompt.append("Return ONLY a valid JSON object with this exact structure:\n");
        prompt.append("{\n");
        prompt.append("  \"project\": {\n");
        prompt.append("    \"name\": \"project name\",\n");
        prompt.append("    \"description\": \"powerful description in portfolio tone (max 500 chars)\",\n");
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