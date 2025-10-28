package com.portfolio.adapter.out.external.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.adapter.out.persistence.jpa.ExperienceJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.ExperienceJpaRepository;
import com.portfolio.adapter.out.persistence.jpa.SkillJpaEntity;
import com.portfolio.adapter.out.persistence.jpa.SkillJpaRepository;
import com.portfolio.core.port.out.AIServicePort;
import com.portfolio.service.ClaudeTokenBudgetService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(AIServiceImpl.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ClaudeTokenBudgetService tokenBudgetService;
    private final SkillJpaRepository skillRepository;
    private final ExperienceJpaRepository experienceRepository;
    private final String anthropicApiKey;
    private final String anthropicApiUrl;
    private final String portfolioToneContextConfig;
    private volatile String portfolioToneContext = null;
    private volatile String portfolioSkillsContext = null;
    private volatile String portfolioExperiencesContext = null;

    public AIServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper,
                        ClaudeTokenBudgetService tokenBudgetService,
                        SkillJpaRepository skillRepository,
                        ExperienceJpaRepository experienceRepository,
                        @Value("${anthropic.api.key:}") String anthropicApiKey,
                        @Value("${anthropic.api.url:https://api.anthropic.com/v1/messages}") String anthropicApiUrl,
                        @Value("${portfolio.ai.context.tone:}") String portfolioToneContextConfig) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tokenBudgetService = tokenBudgetService;
        this.skillRepository = skillRepository;
        this.experienceRepository = experienceRepository;
        this.anthropicApiKey = anthropicApiKey;
        this.anthropicApiUrl = anthropicApiUrl;
        this.portfolioToneContextConfig = portfolioToneContextConfig;

        if (anthropicApiKey != null && !anthropicApiKey.trim().isEmpty()) {
            log.info("Anthropic API key configured successfully");
        } else {
            log.warn("Anthropic API key not configured - will use mock data");
        }
    }

    /**
     * Initialize AI context after all dependencies are injected
     * This method runs after Spring has fully initialized the bean
     */
    @PostConstruct
    public void initializeContext() {
        log.info("Initializing AI portfolio context...");
        try {
            loadPortfolioToneContext();
            loadPortfolioSkillsContext();
            loadPortfolioExperiencesContext();
            log.info("AI portfolio context initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize AI context, will use defaults: {}", e.getMessage(), e);
            // Set defaults to ensure the service can still operate
            if (portfolioToneContext == null) {
                portfolioToneContext = getDefaultPortfolioTone();
            }
            if (portfolioSkillsContext == null) {
                portfolioSkillsContext = "Skills Context: No skills data available yet\n";
            }
            if (portfolioExperiencesContext == null) {
                portfolioExperiencesContext = "Experiences Context: No experience data available yet\n";
            }
        }
    }
    
    /**
     * Load portfolio tone context from application configuration
     * This replaces the previous file system access approach which had security issues
     */
    private void loadPortfolioToneContext() {
        try {
            if (portfolioToneContextConfig != null && !portfolioToneContextConfig.trim().isEmpty()) {
                // Use configured tone context from application.properties
                portfolioToneContext = portfolioToneContextConfig;
                log.info("Portfolio tone context loaded from configuration ({} characters)",
                        portfolioToneContext.length());
            } else {
                // Fall back to default if not configured
                portfolioToneContext = getDefaultPortfolioTone();
                log.warn("No portfolio tone context configured, using default");
            }
        } catch (Exception e) {
            log.error("Error loading portfolio tone context: {}", e.getMessage(), e);
            portfolioToneContext = getDefaultPortfolioTone();
        }
    }
    
    private String getDefaultPortfolioTone() {
        return "Portfolio Tone Context:\n" +
               "- Title: \"Catalytic Architect & Full-Stack Engineer\"\n" +
               "- Hero: \"Your team doesn't need more developers. It needs a phase catalyst.\"\n" +
               "- Sub: \"I break systems that have outgrown their chaos but are not yet ready for stability.\"\n" +
               "- About: \"Technical catalyst and architecture strategist. I expose what is broken and engineer coherence where chaos once reigned. I do not adapt, I transform. I do not deconfigure, I reconfigure.\"\n" +
               "- Key phrases: \"Dissonance sparks transformation\", \"Refactoring cultures drives true development\", \"Code is the output, not the objective\"\n";
    }

    /**
     * Load portfolio skills context from database
     * Provides AI with Bernard's technical skill set for better context matching
     */
    private void loadPortfolioSkillsContext() {
        try {
            List<SkillJpaEntity> skills = skillRepository.findAll();

            if (skills.isEmpty()) {
                log.warn("No skills found in database for AI context");
                portfolioSkillsContext = "Skills Context: No skills data available yet\n";
                return;
            }

            StringBuilder skillsBuilder = new StringBuilder();
            skillsBuilder.append("\n=== BERNARD'S TECHNICAL SKILLS ===\n");
            skillsBuilder.append("Use these to understand Bernard's expertise and suggest relevant skill connections:\n\n");

            // Group by category for better context
            Map<String, List<SkillJpaEntity>> skillsByCategory = skills.stream()
                    .collect(Collectors.groupingBy(skill ->
                            skill.getCategory() != null ? skill.getCategory().toString() : "UNCATEGORIZED"));

            skillsByCategory.forEach((category, categorySkills) -> {
                skillsBuilder.append(String.format("**%s:**\n", category));
                categorySkills.forEach(skill -> {
                    skillsBuilder.append(String.format("  - %s", skill.getName()));
                    if (skill.getLevel() != null) {
                        skillsBuilder.append(String.format(" [Level: %s]", skill.getLevel()));
                    }
                    if (skill.getDescription() != null && !skill.getDescription().isEmpty()) {
                        skillsBuilder.append(String.format(" - %s", skill.getDescription()));
                    }
                    skillsBuilder.append("\n");
                });
                skillsBuilder.append("\n");
            });

            portfolioSkillsContext = skillsBuilder.toString();
            log.info("Portfolio skills context loaded: {} skills across {} categories",
                     skills.size(), skillsByCategory.size());

        } catch (Exception e) {
            log.error("Error loading portfolio skills context: {}", e.getMessage());
            portfolioSkillsContext = "Skills Context: Error loading skills data\n";
        }
    }

    /**
     * Load portfolio experiences context from database
     * Provides AI with Bernard's professional history for better matching
     */
    private void loadPortfolioExperiencesContext() {
        try {
            List<ExperienceJpaEntity> experiences = experienceRepository.findAll();

            if (experiences.isEmpty()) {
                log.warn("No experiences found in database for AI context");
                portfolioExperiencesContext = "Experiences Context: No experience data available yet\n";
                return;
            }

            StringBuilder expBuilder = new StringBuilder();
            expBuilder.append("\n=== BERNARD'S PROFESSIONAL EXPERIENCES ===\n");
            expBuilder.append("Use these to suggest relevant experience connections and career narrative:\n\n");

            // Sort by current position first, then by start date
            experiences.stream()
                    .sorted((e1, e2) -> {
                        if (Boolean.TRUE.equals(e1.getIsCurrentPosition())) return -1;
                        if (Boolean.TRUE.equals(e2.getIsCurrentPosition())) return 1;
                        return 0;
                    })
                    .forEach(exp -> {
                        expBuilder.append(String.format("**%s** at %s",
                                exp.getJobTitle(), exp.getCompanyName()));
                        if (Boolean.TRUE.equals(exp.getIsCurrentPosition())) {
                            expBuilder.append(" [CURRENT]");
                        }
                        expBuilder.append("\n");

                        if (exp.getType() != null) {
                            expBuilder.append(String.format("  Type: %s\n", exp.getType()));
                        }
                        if (exp.getDescription() != null && !exp.getDescription().isEmpty()) {
                            String truncatedDesc = exp.getDescription().length() > 200 ?
                                    exp.getDescription().substring(0, 197) + "..." :
                                    exp.getDescription();
                            expBuilder.append(String.format("  Description: %s\n", truncatedDesc));
                        }
                        expBuilder.append("\n");
                    });

            portfolioExperiencesContext = expBuilder.toString();
            log.info("Portfolio experiences context loaded: {} experiences", experiences.size());

        } catch (Exception e) {
            log.error("Error loading portfolio experiences context: {}", e.getMessage());
            portfolioExperiencesContext = "Experiences Context: Error loading experience data\n";
        }
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

        // Inject Bernard's skills and experiences context for better analysis
        if (portfolioSkillsContext != null) {
            prompt.append(portfolioSkillsContext);
        }
        if (portfolioExperiencesContext != null) {
            prompt.append(portfolioExperiencesContext);
        }
        prompt.append("\nUse Bernard's skills and experiences to suggest relevant connections and validate technical alignment.\n");
        prompt.append("When suggesting skills or experiences, reference existing ones from the context above.\n\n");

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
        prompt.append("6. CRITICAL: Keep project description under 900 characters - be concise but powerful\n");
        prompt.append("7. CRITICAL: Experience job titles must be under 180 characters - be precise and impactful\n\n");
        
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
    
    @Retry(name = "claude", fallbackMethod = "callClaudeApiFallback")
    @CircuitBreaker(name = "claude", fallbackMethod = "callClaudeApiFallback")
    @RateLimiter(name = "claude")
    @TimeLimiter(name = "claude")
    private String callClaudeApi(String prompt) throws Exception {
        // Check token budget before making the call
        int estimatedTokens = estimateTokenUsage(prompt);
        ClaudeTokenBudgetService.BudgetResult budgetResult = tokenBudgetService.useTokens(estimatedTokens, "claude_api_call");
        
        if (!budgetResult.isAllowed()) {
            log.warn("Claude API call blocked due to budget limit: {}", budgetResult.getReason());
            throw new RuntimeException("Claude API budget exceeded: " + budgetResult.getReason());
        }
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
    
    /**
     * Fallback method for Claude API calls
     */
    @SuppressWarnings("unused")
    private String callClaudeApiFallback(String prompt, Exception ex) {
        log.warn("Claude API fallback triggered due to: {}", ex.getMessage());
        
        if (ex.getMessage() != null && ex.getMessage().contains("budget")) {
            return "{\"project\":{\"name\":\"Draft Project\",\"description\":\"AI curation temporarily disabled due to budget limits\",\"technologies\":[],\"url\":\"\"},\"skills\":[],\"experiences\":[]}";
        }
        
        return "{\"project\":{\"name\":\"Draft Project\",\"description\":\"AI curation temporarily unavailable\",\"technologies\":[],\"url\":\"\"},\"skills\":[],\"experiences\":[]}";
    }
    
    /**
     * Estimate token usage for a prompt (rough estimation)
     */
    private int estimateTokenUsage(String prompt) {
        if (prompt == null) return 0;
        
        // Rough estimation: 1 token ≈ 4 characters for English text
        // Add overhead for response tokens (typically 2-3x the prompt)
        int promptTokens = prompt.length() / 4;
        int responseTokens = promptTokens * 2; // Conservative estimate
        
        return promptTokens + responseTokens;
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

        // Inject Bernard's skills and experiences context
        if (portfolioSkillsContext != null) {
            prompt.append(portfolioSkillsContext);
        }
        if (portfolioExperiencesContext != null) {
            prompt.append(portfolioExperiencesContext);
        }
        prompt.append("\nUse Bernard's background to create a summary that reflects his expertise and experience.\n\n");

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

        // Inject Bernard's skills and experiences context
        if (portfolioSkillsContext != null) {
            prompt.append(portfolioSkillsContext);
        }
        if (portfolioExperiencesContext != null) {
            prompt.append(portfolioExperiencesContext);
        }
        prompt.append("\nReference Bernard's expertise when crafting the message about these technologies.\n\n");

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
     * Safely truncates job title to fit within database constraints
     * @param jobTitle The AI-generated job title
     * @param fallback Fallback job title if truncation results in empty string
     * @return Job title that fits within 200 character limit
     */
    public String truncateJobTitle(String jobTitle, String fallback) {
        if (jobTitle == null || jobTitle.trim().isEmpty()) {
            return fallback != null ? fallback : "Software Developer";
        }
        
        String cleanJobTitle = jobTitle.trim();
        
        // If within limit, return as-is
        if (cleanJobTitle.length() <= 200) {
            return cleanJobTitle;
        }
        
        // Truncate intelligently at word boundary if possible
        String truncated = cleanJobTitle.substring(0, 197); // Leave room for "..."
        
        // Try to end at word boundary
        int lastSpace = truncated.lastIndexOf(' ');
        if (lastSpace > 100) { // Only use word boundary if it's not too short
            truncated = truncated.substring(0, lastSpace) + "...";
        } else {
            truncated = truncated + "...";
        }
        
        log.debug("Truncated job title from {} to {} characters", cleanJobTitle.length(), truncated.length());
        return truncated;
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
        int lastSentenceEnd = Math.max(truncated.lastIndexOf(". "), truncated.lastIndexOf("! "));
        lastSentenceEnd = Math.max(lastSentenceEnd, truncated.lastIndexOf("? "));
        
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
    
    /**
     * General chat method for AI interactions
     * @param systemPrompt System-level instructions for the AI
     * @param userPrompt User message or prompt
     * @return AI response
     */
    public String chat(String systemPrompt, String userPrompt) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            log.warn("Empty user prompt provided to chat method");
            return "No se proporcionó una consulta válida.";
        }
        
        if (anthropicApiKey == null || anthropicApiKey.trim().isEmpty()) {
            log.warn("Claude API key not configured, returning default response");
            return "El análisis AI no está disponible en este momento.";
        }
        
        try {
            String fullPrompt = buildChatPrompt(systemPrompt, userPrompt);
            String response = callClaudeApi(fullPrompt);
            
            if (response != null && !response.trim().isEmpty()) {
                return response.trim();
            } else {
                return "No se pudo generar una respuesta válida.";
            }
            
        } catch (Exception e) {
            log.error("Error in chat API call", e);
            return "Error generando respuesta AI: " + e.getMessage();
        }
    }
    
    private String buildChatPrompt(String systemPrompt, String userPrompt) {
        StringBuilder prompt = new StringBuilder();
        
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            prompt.append(systemPrompt.trim()).append("\n\n");
        }
        
        prompt.append(userPrompt.trim());
        
        return prompt.toString();
    }
}
