package com.casrusil.SII_ERP_AI.shared.test;

import com.casrusil.SII_ERP_AI.SiiErpAiApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using Testcontainers.
 * Provides a shared PostgreSQL container for all integration tests.
 */
@SpringBootTest(classes = SiiErpAiApplication.class)
@Testcontainers
public abstract class BaseIntegrationTest {

    /**
     * Shared PostgreSQL container (singleton pattern).
     * Container is reused across all tests in the same JVM for performance.
     */
    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("sii_erp_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    /**
     * Configure Spring datasource properties dynamically from the container.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // JPA configuration for tests
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
    }
}
