package com.portfolio.mock;

import com.portfolio.core.port.out.AIServicePort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.List;

/**
 * Mock Claude AI Service for integration testing.
 *
 * Provides deterministic, realistic AI responses without consuming API tokens.
 * All methods return pre-defined responses based on input patterns.
 *
 * @author Bernard Uriza Orozco
 */
@TestConfiguration
public class MockClaudeService {

    @Bean
    @Primary
    public AIServicePort mockAIServicePort() {
        return new AIServicePort() {

            @Override
            public String generateProjectSummary(String title, String description, String technologies) {
                return String.format("AI-generated summary for %s using %s: %s",
                        title, technologies, description != null ? description : "No description provided");
            }

            @Override
            public String generateDynamicMessage(String technologies) {
                return "Explore cutting-edge projects built with " + technologies +
                        ", showcasing modern development practices and innovative solutions.";
            }

            @Override
            public String analyzeTechnologies(List<String> technologies) {
                if (technologies.isEmpty()) {
                    return "No technologies provided for analysis.";
                }
                return "Analysis of technologies: " + String.join(", ", technologies) +
                        ". These technologies represent a modern, production-ready stack.";
            }

            @Override
            public ClaudeAnalysisResult analyzeRepository(String repoName, String description,
                                                          String readmeContent, List<String> topics, String language) {
                // Mock project data
                ProjectData project = new ProjectData(
                        repoName,
                        description != null ? description : "Mock repository for testing purposes",
                        4, // 4 weeks duration
                        topics != null && !topics.isEmpty() ? topics : Arrays.asList(language, "testing"),
                        "https://github.com/testuser/" + repoName
                );

                // Extract skills from language and topics
                List<String> skills = Arrays.asList(
                        language != null ? language : "Java",
                        "Git",
                        "Testing"
                );

                // Mock experiences
                List<String> experiences = Arrays.asList(
                        "Developed " + repoName + " using " + (language != null ? language : "modern technologies"),
                        "Implemented testing strategies for " + repoName
                );

                return new ClaudeAnalysisResult(project, skills, experiences);
            }

            @Override
            public String chat(String systemPrompt, String userPrompt) {
                // Simple mock response based on user prompt keywords
                if (userPrompt.toLowerCase().contains("test")) {
                    return "This is a mock AI response for testing purposes. The system understands: " + systemPrompt;
                } else if (userPrompt.toLowerCase().contains("analyze")) {
                    return "Analysis complete. Mock AI has processed the request successfully.";
                } else {
                    return "Mock AI response to: " + userPrompt;
                }
            }
        };
    }
}
