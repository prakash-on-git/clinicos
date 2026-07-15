package com.prakash.clinicos;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

/**
 * Base class for all integration tests.
 *
 * Design decisions:
 *
 * 1. @Testcontainers(disabledWithoutDocker = true)
 *    If Docker is not available on the machine (e.g. in a CI environment without Docker,
 *    or on a dev machine where Docker is stopped), integration tests are SKIPPED cleanly
 *    instead of failing with an ugly error. Unit tests are unaffected — they never extend
 *    this class and have no Docker dependency.
 *    To run integration tests: ensure Docker is running, then ./mvnw test
 *
 * 2. @Container static — singleton container per test class
 *    Testcontainers starts the PostgreSQL container before the JUnit 5 @BeforeAll phase
 *    (before Spring loads the ApplicationContext). The @DynamicPropertySource then redirects
 *    the datasource to the running container. Spring caches contexts per property set,
 *    so subclasses that share the same container share the same Spring context too.
 *
 * 3. webEnvironment = MOCK
 *    Requests are dispatched in the same thread as the test method, which is required for
 *    @Transactional to roll back after each test and keep tests isolated.
 *
 * 4. MockMvc built via MockMvcBuilders.webAppContextSetup()
 *    Spring Boot 4.x removed @AutoConfigureMockMvc. We build MockMvc manually
 *    with the full Spring Security filter chain applied.
 *
 * 5. Flyway runs on the fresh container DB on first startup — all 13 migrations execute,
 *    including the V2 role seeds (CLINIC_ADMIN, PATIENT, SUPER_ADMIN).
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL 16 container.
     *
     * @Container static → Testcontainers starts it once per test class before
     * SpringExtension loads the ApplicationContext. Each subclass gets its own
     * container lifecycle (started/stopped around the full class run).
     */
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    /**
     * Redirect the Spring datasource to the Testcontainers PostgreSQL instance.
     * Called by Spring AFTER Testcontainers starts the container (guaranteed by
     * extension ordering in JUnit 5: TestcontainersExtension → SpringExtension).
     */
    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private WebApplicationContext applicationContext;

    /** Jackson 3.x ObjectMapper from the Spring context. */
    @Autowired
    protected ObjectMapper objectMapper;

    /** Rebuilt before each test with the full Spring Security filter chain. */
    protected MockMvc mockMvc;

    @BeforeEach
    void buildMockMvc() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }
}
