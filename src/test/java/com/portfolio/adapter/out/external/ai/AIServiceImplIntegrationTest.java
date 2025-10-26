package com.portfolio.adapter.out.external.ai;

import com.portfolio.config.TestContainersConfiguration;
import com.portfolio.core.port.out.AIServicePort;
import com.portfolio.mock.MockClaudeService;
import com.portfolio.service.ClaudeTokenBudgetService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for AIServiceImpl (Claude AI integration).
 *
 * Tests the AI service port implementation without making real API calls.
 * Uses the test profile which has AI features disabled, so these tests
 * verify the service layer integration, error handling, and fallback behavior.
 *
 * @author Bernard Uriza Orozco
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import({TestContainersConfiguration.class, MockClaudeService.class})
@Disabled("TODO: Fix AI mock assertions for CI/CD - see Trello card")
class AIServiceImplIntegrationTest {

    @Autowired
    private AIServicePort aiService;

    @Autowired(required = false)
    private ClaudeTokenBudgetService tokenBudgetService;

    @Test
    void testAIServicePortBeanExists() {
        // Verify AI service bean is properly injected
        assertThat(aiService).isNotNull();
    }

    @Test
    void testGenerateProjectSummary_WithValidInputs() {
        // Given: Valid project data
        String title = "Spring Boot REST API";
        String description = "A comprehensive REST API built with Spring Boot";
        String technologies = "Java, Spring Boot, PostgreSQL";

        // When: Generate summary (test profile uses mock)
        String summary = aiService.generateProjectSummary(title, description, technologies);

        // Then: Summary is generated
        assertThat(summary).isNotNull();
        assertThat(summary).isNotEmpty();
        assertThat(summary).contains(title);
    }

    @Test
    void testGenerateProjectSummary_WithNullDescription() {
        // Given: Null description
        String title = "Test Project";
        String description = null;
        String technologies = "Java";

        // When: Generate summary
        String summary = aiService.generateProjectSummary(title, description, technologies);

        // Then: Summary is still generated
        assertThat(summary).isNotNull();
        assertThat(summary).contains(title);
    }

    @Test
    void testGenerateDynamicMessage_WithTechnologies() {
        // Given: Technology stack
        String technologies = "React, TypeScript, Node.js";

        // When: Generate dynamic message
        String message = aiService.generateDynamicMessage(technologies);

        // Then: Message is generated
        assertThat(message).isNotNull();
        assertThat(message).isNotEmpty();
        assertThat(message.toLowerCase()).contains("technologies");
    }

    @Test
    void testAnalyzeTechnologies_WithMultipleTechnologies() {
        // Given: List of technologies
        List<String> technologies = Arrays.asList("Java", "Spring Boot", "PostgreSQL", "Docker");

        // When: Analyze technologies
        String analysis = aiService.analyzeTechnologies(technologies);

        // Then: Analysis is generated
        assertThat(analysis).isNotNull();
        assertThat(analysis).isNotEmpty();
    }

    @Test
    void testAnalyzeTechnologies_WithEmptyList() {
        // Given: Empty technology list
        List<String> technologies = List.of();

        // When: Analyze technologies
        String analysis = aiService.analyzeTechnologies(technologies);

        // Then: Analysis handles empty input gracefully
        assertThat(analysis).isNotNull();
        assertThat(analysis).contains("No technologies");
    }

    @Test
    void testAnalyzeRepository_WithValidData() {
        // Given: Repository data
        String repoName = "portfolio-backend";
        String description = "Spring Boot backend for portfolio";
        String readmeContent = "# Portfolio Backend\n\nA comprehensive backend API...";
        List<String> topics = Arrays.asList("spring-boot", "rest-api", "postgresql");
        String language = "Java";

        // When: Analyze repository
        AIServicePort.ClaudeAnalysisResult result = aiService.analyzeRepository(
                repoName, description, readmeContent, topics, language);

        // Then: Analysis result is structured
        assertThat(result).isNotNull();
        assertThat(result.project).isNotNull();
        assertThat(result.skills).isNotNull();
        assertThat(result.experiences).isNotNull();

        // Verify project data
        assertThat(result.project.name).isEqualTo(repoName);
        assertThat(result.project.technologies).isNotEmpty();

        // Verify skills extracted
        assertThat(result.skills).isNotEmpty();
        assertThat(result.skills).contains(language);

        // Verify experiences extracted
        assertThat(result.experiences).isNotEmpty();
    }

    @Test
    void testAnalyzeRepository_WithNullDescription() {
        // Given: Repository data with null description
        String repoName = "test-repo";
        String description = null;
        String readmeContent = "# Test Repository";
        List<String> topics = Arrays.asList("testing");
        String language = "Python";

        // When: Analyze repository
        AIServicePort.ClaudeAnalysisResult result = aiService.analyzeRepository(
                repoName, description, readmeContent, topics, language);

        // Then: Analysis handles null description
        assertThat(result).isNotNull();
        assertThat(result.project).isNotNull();
        assertThat(result.project.description).isNotNull();
    }

    @Test
    void testAnalyzeRepository_WithEmptyTopics() {
        // Given: Repository data with empty topics
        String repoName = "minimal-repo";
        String description = "Minimal repository";
        String readmeContent = null;
        List<String> topics = List.of();
        String language = "JavaScript";

        // When: Analyze repository
        AIServicePort.ClaudeAnalysisResult result = aiService.analyzeRepository(
                repoName, description, readmeContent, topics, language);

        // Then: Analysis handles empty topics
        assertThat(result).isNotNull();
        assertThat(result.project.technologies).isNotEmpty(); // At least language should be there
    }

    @Test
    void testChat_WithSystemAndUserPrompts() {
        // Given: System and user prompts
        String systemPrompt = "You are a helpful coding assistant.";
        String userPrompt = "Explain Spring Boot in one sentence.";

        // When: Chat with AI
        String response = aiService.chat(systemPrompt, userPrompt);

        // Then: Response is generated
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }

    @Test
    void testChat_WithTestKeyword() {
        // Given: Prompt containing "test"
        String systemPrompt = "You are a test assistant.";
        String userPrompt = "This is a test prompt.";

        // When: Chat with AI
        String response = aiService.chat(systemPrompt, userPrompt);

        // Then: Response acknowledges test context
        assertThat(response).isNotNull();
        assertThat(response.toLowerCase()).containsAnyOf("test", "mock");
    }

    @Test
    void testChat_WithAnalyzeKeyword() {
        // Given: Prompt containing "analyze"
        String systemPrompt = "You are an analysis assistant.";
        String userPrompt = "Analyze this code structure.";

        // When: Chat with AI
        String response = aiService.chat(systemPrompt, userPrompt);

        // Then: Response indicates analysis
        assertThat(response).isNotNull();
        assertThat(response.toLowerCase()).containsAnyOf("analysis", "analyze", "mock");
    }

    @Test
    void testTokenBudgetService_Integration() {
        // Verify token budget service is available (may be null in test profile)
        if (tokenBudgetService != null) {
            assertThat(tokenBudgetService.getRemainingTokens()).isGreaterThanOrEqualTo(0);
            assertThat(tokenBudgetService.getCurrentUsage()).isGreaterThanOrEqualTo(0);
            assertThat(tokenBudgetService.getUsagePercentage()).isBetween(0.0, 100.0);
        } else {
            // Token budget disabled in test profile is acceptable
            assertThat(tokenBudgetService).isNull();
        }
    }

    @Test
    void testClaudeAnalysisResult_StructureIntegrity() {
        // Given: Create analysis result
        AIServicePort.ProjectData projectData = new AIServicePort.ProjectData(
                "Test Project",
                "Test Description",
                8,
                Arrays.asList("Java", "Spring"),
                "https://github.com/test/project"
        );

        List<String> skills = Arrays.asList("Java", "Spring Boot", "Testing");
        List<String> experiences = Arrays.asList("Built REST API", "Implemented caching");

        AIServicePort.ClaudeAnalysisResult result = new AIServicePort.ClaudeAnalysisResult(
                projectData, skills, experiences);

        // Then: Verify structure
        assertThat(result.project).isEqualTo(projectData);
        assertThat(result.skills).hasSize(3);
        assertThat(result.experiences).hasSize(2);
        assertThat(result.project.estimatedDurationWeeks).isEqualTo(8);
    }

    @Test
    void testProjectData_AllFieldsAccessible() {
        // Given: Create project data
        AIServicePort.ProjectData project = new AIServicePort.ProjectData(
                "Spring Boot API",
                "Comprehensive REST API",
                12,
                Arrays.asList("Java 21", "Spring Boot 3.5", "PostgreSQL 16"),
                "https://github.com/example/spring-api"
        );

        // Then: All fields are accessible
        assertThat(project.name).isEqualTo("Spring Boot API");
        assertThat(project.description).isEqualTo("Comprehensive REST API");
        assertThat(project.estimatedDurationWeeks).isEqualTo(12);
        assertThat(project.technologies).hasSize(3);
        assertThat(project.url).startsWith("https://github.com");
    }
}
