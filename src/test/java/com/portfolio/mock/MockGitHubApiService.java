package com.portfolio.mock;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Base64;

/**
 * Mock GitHub API service for integration testing.
 *
 * Provides realistic GitHub API responses without hitting the actual API.
 * Uses OkHttp's MockWebServer to simulate GitHub REST API endpoints.
 *
 * @author Bernard Uriza Orozco
 */
public class MockGitHubApiService {

    private final MockWebServer mockWebServer;

    public MockGitHubApiService() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
    }

    /**
     * Get the base URL for the mock GitHub API server.
     *
     * @return Base URL (e.g., "http://localhost:12345")
     */
    public String getBaseUrl() {
        return mockWebServer.url("/").toString();
    }

    /**
     * Enqueue a successful response for starred repositories.
     *
     * Returns a list of 3 mock starred repositories with realistic data.
     */
    public void enqueueStarredRepositoriesResponse() {
        String jsonResponse = """
                [
                    {
                        "id": 123456789,
                        "name": "test-repo-1",
                        "full_name": "testuser/test-repo-1",
                        "description": "A test repository for integration testing",
                        "html_url": "https://github.com/testuser/test-repo-1",
                        "language": "Java",
                        "topics": ["spring-boot", "testing", "java"],
                        "stargazers_count": 42,
                        "forks_count": 10,
                        "created_at": "2024-01-15T10:30:00Z",
                        "updated_at": "2024-10-25T12:00:00Z",
                        "pushed_at": "2024-10-24T18:45:00Z"
                    },
                    {
                        "id": 987654321,
                        "name": "mock-api-lib",
                        "full_name": "testuser/mock-api-lib",
                        "description": "Mock API library for testing",
                        "html_url": "https://github.com/testuser/mock-api-lib",
                        "language": "TypeScript",
                        "topics": ["api", "mock", "testing"],
                        "stargazers_count": 128,
                        "forks_count": 25,
                        "created_at": "2023-06-10T08:20:00Z",
                        "updated_at": "2024-10-20T14:30:00Z",
                        "pushed_at": "2024-10-18T16:00:00Z"
                    },
                    {
                        "id": 555666777,
                        "name": "portfolio-example",
                        "full_name": "testuser/portfolio-example",
                        "description": null,
                        "html_url": "https://github.com/testuser/portfolio-example",
                        "language": "Python",
                        "topics": [],
                        "stargazers_count": 5,
                        "forks_count": 1,
                        "created_at": "2024-09-01T09:00:00Z",
                        "updated_at": "2024-09-15T11:00:00Z",
                        "pushed_at": "2024-09-14T20:30:00Z"
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(jsonResponse));
    }

    /**
     * Enqueue a successful empty response (no starred repositories).
     */
    public void enqueueEmptyStarredRepositoriesResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("[]"));
    }

    /**
     * Enqueue a 429 rate limit exceeded response.
     */
    public void enqueueRateLimitResponse() {
        String rateLimitJson = """
                {
                    "message": "API rate limit exceeded",
                    "documentation_url": "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setHeader("X-RateLimit-Limit", "60")
                .setHeader("X-RateLimit-Remaining", "0")
                .setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + 3600))
                .setBody(rateLimitJson));
    }

    /**
     * Enqueue a 500 server error response.
     */
    public void enqueueServerErrorResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\": \"Internal server error\"}"));
    }

    /**
     * Enqueue a successful README response.
     *
     * @param repoName Repository name for the README
     */
    public void enqueueReadmeResponse(String repoName) {
        String readmeContent = "# " + repoName + "\n\nThis is a sample README file for testing purposes.\n\n## Features\n\n- Feature 1\n- Feature 2\n- Feature 3\n";
        String base64Content = Base64.getEncoder().encodeToString(readmeContent.getBytes());

        String readmeJson = String.format("""
                {
                    "name": "README.md",
                    "path": "README.md",
                    "sha": "abc123def456",
                    "size": %d,
                    "content": "%s",
                    "encoding": "base64"
                }
                """, readmeContent.length(), base64Content);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(readmeJson));
    }

    /**
     * Enqueue a 404 response for missing README.
     */
    public void enqueueReadmeNotFoundResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\": \"Not Found\"}"));
    }

    /**
     * Shutdown the mock web server.
     *
     * @throws IOException if shutdown fails
     */
    public void shutdown() throws IOException {
        mockWebServer.shutdown();
    }
}
