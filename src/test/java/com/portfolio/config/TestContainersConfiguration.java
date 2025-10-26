package com.portfolio.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * TestContainers configuration for integration tests.
 *
 * Provides a PostgreSQL container that automatically integrates
 * with Spring Boot's DataSource configuration.
 *
 * @author Bernard Uriza Orozco
 * @see <a href="https://www.testcontainers.org/">TestContainers Documentation</a>
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfiguration {

    private static final PostgreSQLContainer<?> postgresContainer;

    static {
        postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("portfolio_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);  // Reuse container across tests for performance

        postgresContainer.start();
    }

    /**
     * Dynamically configures Spring datasource properties from the PostgreSQL container.
     *
     * @param registry Dynamic property registry for test properties
     */
    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Bean
    public PostgreSQLContainer<?> postgresContainer() {
        return postgresContainer;
    }
}

