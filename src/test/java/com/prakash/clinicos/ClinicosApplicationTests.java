package com.prakash.clinicos;

import org.junit.jupiter.api.Test;

/**
 * Smoke test — verifies the entire Spring ApplicationContext loads successfully
 * against a real PostgreSQL database (provided by Testcontainers via AbstractIntegrationTest).
 *
 * If this fails it means a bean is misconfigured, a migration is broken,
 * or a required @Value / @ConfigurationProperties is missing.
 */
class ClinicosApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        // Spring context loaded successfully if we reach here.
        // All 13 Flyway migrations ran. All beans wired. All @Value fields resolved.
    }
}
