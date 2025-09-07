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
            
            StringBuilder toneBuilder = new StringBuilder();
            toneBuilder.append("Bernard Uriza's Portfolio Tone & Style Context:\n\n");
            
            // Load index.html meta information
            java.nio.file.Path indexPath = java.nio.file.Paths.get(frontendPath, "src", "index.html");
            if (java.nio.file.Files.exists(indexPath)) {
                String indexContent = new String(java.nio.file.Files.readAllBytes(indexPath), java.nio.charset.StandardCharsets.UTF_8);
                if (indexContent.contains("Catalytic Architect")) {
                    toneBuilder.append("Professional Identity: Catalytic Architect & Full-Stack Engineer\n");
                }
                if (indexContent.contains("technical transformation")) {
                    toneBuilder.append("Mission Statement: Architect of technical transformation. Design and execute software systems that catalyze change.\n\n");
                }
            }
            
            // Load comprehensive i18n content
            java.nio.file.Path i18nPath = java.nio.file.Paths.get(frontendPath, "src", "app", "core", "i18n.service.ts");
            if (java.nio.file.Files.exists(i18nPath)) {
                String i18nContent = new String(java.nio.file.Files.readAllBytes(i18nPath), java.nio.charset.StandardCharsets.UTF_8);
                
                // Hero message and philosophy
                toneBuilder.append("CORE MESSAGING:\n");
                if (i18nContent.contains("Your team doesn't need more developers")) {
                    toneBuilder.append("- Hero Statement: \"Your team doesn't need more developers. It needs a phase catalyst.\"\n");
                }
                if (i18nContent.contains("I break systems that have outgrown their chaos")) {
                    toneBuilder.append("- Core Philosophy: \"I break systems that have outgrown their chaos but are not yet ready for stability.\"\n");
                }
                
                // About section identity
                if (i18nContent.contains("Technical catalyst and architecture strategist")) {
                    toneBuilder.append("- Professional Identity: \"Technical catalyst and architecture strategist. I expose what is broken and engineer coherence where chaos once reigned. I do not adapt, I transform. I do not decorate, I reconfigure.\"\n\n");
                }
                
                // Key phrases/mantras
                toneBuilder.append("KEY MANTRAS:\n");
                if (i18nContent.contains("Dissonance sparks transformation")) {
                    toneBuilder.append("- \"Dissonance sparks transformation\"\n");
                }
                if (i18nContent.contains("Refactoring cultures drives true development")) {
                    toneBuilder.append("- \"Refactoring cultures drives true development\"\n");
                }
                if (i18nContent.contains("Code is the output, not the objective")) {
                    toneBuilder.append("- \"Code is the output, not the objective\"\n");
                }
                if (i18nContent.contains("Architecting beyond the codebase")) {
                    toneBuilder.append("- \"Architecting beyond the codebase\"\n");
                }
                if (i18nContent.contains("Cyber-resilience through catalytic design")) {
                    toneBuilder.append("- \"Cyber-resilience through catalytic design\"\n\n");
                }
                
                // Service approach
                toneBuilder.append("APPROACH & METHODOLOGY:\n");
                if (i18nContent.contains("I engineer safe collapse")) {
                    toneBuilder.append("- \"I engineer safe collapse. I break the parts that are silently holding you back.\"\n");
                }
                if (i18nContent.contains("I leave when I'm no longer needed")) {
                    toneBuilder.append("- \"I leave when I'm no longer needed. I don't grow with your org. I evolve it.\"\n");
                }
                if (i18nContent.contains("I won't make you feel comfortable")) {
                    toneBuilder.append("- \"I won't make you feel comfortable. I'll make you feel clear.\"\n\n");
                }
                
                // Transformation process
                if (i18nContent.contains("Crisis becomes opportunity")) {
                    toneBuilder.append("TRANSFORMATION PHILOSOPHY:\n");
                    toneBuilder.append("- \"Crisis becomes opportunity when you embrace necessary dissonance\"\n");
                    toneBuilder.append("- \"Only a catalytic shock can realign your organizational story\"\n\n");
                }
            }
            
            // Final style guidance
            toneBuilder.append("TONE REQUIREMENTS:\n");
            toneBuilder.append("- Bold, transformative, confident language\n");
            toneBuilder.append("- Focus on systemic change and catalytic intervention\n");
            toneBuilder.append("- Emphasize breaking through chaos to achieve coherence\n");
            toneBuilder.append("- Use technical precision with philosophical depth\n");
            toneBuilder.append("- Avoid generic development descriptions - focus on transformation\n\n");
            
            portfolioToneContext = toneBuilder.toString();
            log.info("Portfolio tone context loaded successfully with {} characters", portfolioToneContext.length());
            
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
        
        if (anthropicApiKey == null || anthropicApiKey.trim().isEmpty()) {
            log.warn("Claude API key not configured, returning basic summary");
            String safeDescription = description != null ? description : "No description available";
            String safeTechnologies = technologies != null ? technologies : "No technologies specified";
            
            return String.format("Transformative project '%s' leveraging %s for systemic impact. %s", 
                    title, safeTechnologies, safeDescription.length() > 100 ? 
                    safeDescription.substring(0, 100) + "..." : safeDescription);
        }
        
        try {
            String prompt = buildProjectSummaryPrompt(title, description, technologies);
            String response = callClaudeApi(prompt);
            return parseProjectSummaryResponse(response, title, description, technologies);
        } catch (Exception e) {
            log.error("Error generating project summary for '{}': {}", title, e.getMessage());
            return String.format("Catalytic project '%s' demonstrating architectural excellence through %s", 
                    title, technologies != null ? technologies : "cutting-edge technology");
        }
    }
    
    public String generateDynamicMessage(String technologies) {
        log.info("Generating dynamic message for technologies: {}", technologies);
        
        if (anthropicApiKey == null || anthropicApiKey.trim().isEmpty()) {
            log.warn("Claude API key not configured, returning catalytic message");
            return String.format("Your project catalyzes change through %s. " +
                    "This technology stack doesn't just build solutions—it transforms systems and breaks through complexity.", 
                    technologies != null ? technologies : "strategic technology choices");
        }
        
        try {
            String prompt = buildDynamicMessagePrompt(technologies);
            String response = callClaudeApi(prompt);
            return parseDynamicMessageResponse(response, technologies);
        } catch (Exception e) {
            log.error("Error generating dynamic message for technologies '{}': {}", technologies, e.getMessage());
            return String.format("These technologies (%s) are instruments of transformation. " +
                    "They don't just solve problems—they reconfigure possibilities.", 
                    technologies != null ? technologies : "your chosen stack");
        }
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
        prompt.append("5. Experiences should reflect leadership and transformation\n");
        prompt.append("6. CRITICAL: Keep project description under 900 characters - be concise but powerful\n\n");
        
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
            String rawDescription = projectNode.path("description").asText(fallbackDescription);
            String safeDescription = truncateDescription(rawDescription, fallbackDescription);
            
            AIServicePort.ProjectData projectData = new AIServicePort.ProjectData(
                projectNode.path("name").asText(fallbackName),
                safeDescription,
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
        
        String safeDescription = truncateDescription(description, "GitHub repository: " + repoName);
        AIServicePort.ProjectData projectData = new AIServicePort.ProjectData(
            repoName,
            safeDescription,
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
    
    private String buildProjectSummaryPrompt(String title, String description, String technologies) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are creating content for Bernard Uriza's portfolio website.\n\n");
        prompt.append(portfolioToneContext != null ? portfolioToneContext : getDefaultPortfolioTone());
        prompt.append("\n");
        
        prompt.append("Create a powerful, catalytic summary for this project that matches the portfolio's transformative tone:\n\n");
        prompt.append("Project Title: ").append(title).append("\n");
        prompt.append("Description: ").append(description != null ? description : "No description provided").append("\n");
        prompt.append("Technologies: ").append(technologies != null ? technologies : "Not specified").append("\n\n");
        
        prompt.append("Requirements:\n");
        prompt.append("- Write in Bernard's bold, transformative voice\n");
        prompt.append("- Focus on architectural significance and systemic impact\n");
        prompt.append("- Use decisive, powerful language that avoids generic development terms\n");
        prompt.append("- CRITICAL: Maximum 180 characters - be concise and impactful\n");
        prompt.append("- Return ONLY the summary text, no explanations\n");
        
        return prompt.toString();
    }
    
    private String buildDynamicMessagePrompt(String technologies) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are creating content for Bernard Uriza's portfolio website.\n\n");
        prompt.append(portfolioToneContext != null ? portfolioToneContext : getDefaultPortfolioTone());
        prompt.append("\n");
        
        prompt.append("Create an engaging, catalytic message about these technologies that matches the portfolio's tone:\n\n");
        prompt.append("Technologies: ").append(technologies != null ? technologies : "Modern technology stack").append("\n\n");
        
        prompt.append("Requirements:\n");
        prompt.append("- Write as Bernard's AI representative\n");
        prompt.append("- Focus on transformation and systemic change potential\n");
        prompt.append("- Avoid generic tech praise - focus on architectural and catalytic aspects\n");
        prompt.append("- Maximum 250 characters\n");
        prompt.append("- Return ONLY the message text, no explanations\n");
        
        return prompt.toString();
    }
    
    private String parseProjectSummaryResponse(String response, String fallbackTitle, String fallbackDescription, String fallbackTech) {
        try {
            // Try to parse as JSON first, but if it's plain text, use it directly
            if (response.trim().startsWith("{")) {
                JsonNode jsonNode = objectMapper.readTree(response);
                if (jsonNode.has("summary")) {
                    return jsonNode.path("summary").asText();
                }
            }
            
            // Use response as-is if it looks like a summary
            String cleaned = response.trim().replaceAll("^[\"']|[\"']$", "");
            if (cleaned.length() > 10 && cleaned.length() <= 400) {
                return cleaned;
            }
            
        } catch (Exception e) {
            log.debug("Could not parse Claude response as JSON, using fallback");
        }
        
        // Fallback
        return String.format("Transformative project '%s' leveraging %s for catalytic impact.", 
                fallbackTitle, fallbackTech != null ? fallbackTech : "cutting-edge technology");
    }
    
    private String parseDynamicMessageResponse(String response, String fallbackTech) {
        try {
            // Try to parse as JSON first, but if it's plain text, use it directly
            if (response.trim().startsWith("{")) {
                JsonNode jsonNode = objectMapper.readTree(response);
                if (jsonNode.has("message")) {
                    return jsonNode.path("message").asText();
                }
            }
            
            // Use response as-is if it looks like a message
            String cleaned = response.trim().replaceAll("^[\"']|[\"']$", "");
            if (cleaned.length() > 10 && cleaned.length() <= 500) {
                return cleaned;
            }
            
        } catch (Exception e) {
            log.debug("Could not parse Claude response as JSON, using fallback");
        }
        
        // Fallback
        return String.format("These technologies (%s) are instruments of systemic transformation. " +
                "They don't just solve problems—they reconfigure architectural possibilities.", 
                fallbackTech != null ? fallbackTech : "your chosen stack");
    }
    
    /**
     * Safely truncates description to fit within database constraints
     * @param description The AI-generated description
     * @param fallback Fallback description if truncation results in empty string
     * @return Description that fits within 1000 character limit
     */
    private String truncateDescription(String description, String fallback) {
        if (description == null || description.trim().isEmpty()) {
            return fallback != null ? fallback : "GitHub repository";
        }
        
        String cleanDescription = description.trim();
        
        // If within limit, return as-is
        if (cleanDescription.length() <= 1000) {
            return cleanDescription;
        }
        
        // Truncate intelligently at sentence boundary if possible
        String truncated = cleanDescription.substring(0, 997); // Leave room for "..."
        
        // Try to end at sentence boundary
        int lastSentenceEnd = Math.max(truncated.lastIndexOf('. '), truncated.lastIndexOf('! '));
        lastSentenceEnd = Math.max(lastSentenceEnd, truncated.lastIndexOf('? '));
        
        if (lastSentenceEnd > 500) { // Only use sentence boundary if it's not too short
            truncated = truncated.substring(0, lastSentenceEnd + 1);
        } else {
            // Fallback: truncate at word boundary
            int lastSpace = truncated.lastIndexOf(' ');
            if (lastSpace > 500) {
                truncated = truncated.substring(0, lastSpace) + "...";
            } else {
                truncated = truncated + "...";
            }
        }
        
        log.debug("Truncated description from {} to {} characters", cleanDescription.length(), truncated.length());
        return truncated;
    }
}