package com.prakash.clinicos.auth;

import com.prakash.clinicos.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Authentication API.
 *
 * Uses a real PostgreSQL database (Testcontainers) and the full Spring Security
 * filter chain. Every request goes through JWT filter → controller → service → DB.
 *
 * @Transactional rolls back all DB changes after each test method,
 * so every test starts with a clean state.
 */
@Transactional
class AuthIntegrationTest extends AbstractIntegrationTest {

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL    = "/api/v1/auth/login";

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    void register_validRequest_returns201WithAccessToken() throws Exception {
        Map<String, String> body = Map.of(
                "fullName", "Test Admin",
                "email",    uniqueEmail(),
                "password", "password123"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.roles[0]").value("CLINIC_ADMIN"));
    }

    @Test
    void register_duplicateEmail_returns409Conflict() throws Exception {
        String email = uniqueEmail();
        Map<String, String> body = Map.of(
                "fullName", "Test Admin",
                "email",    email,
                "password", "password123"
        );

        // First registration succeeds
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // Second registration with same email → 409 Conflict
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email is already registered"));
    }

    @Test
    void register_missingRequiredFields_returns422() throws Exception {
        // Missing email and password — Bean Validation should reject with 422
        Map<String, String> body = Map.of("fullName", "No Email");

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors").exists());
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200WithAccessToken() throws Exception {
        String email    = uniqueEmail();
        String password = "password123";

        // Register first
        Map<String, String> regBody = Map.of(
                "fullName", "Login Test User",
                "email",    email,
                "password", password
        );
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regBody)))
                .andExpect(status().isCreated());

        // Then login
        Map<String, String> loginBody = Map.of("email", email, "password", password);
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void login_wrongPassword_returns401Unauthorized() throws Exception {
        String email = uniqueEmail();

        // Register first
        Map<String, String> regBody = Map.of(
                "fullName", "Auth Test User",
                "email",    email,
                "password", "correct-password"
        );
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regBody)))
                .andExpect(status().isCreated());

        // Login with wrong password → 401
        Map<String, String> loginBody = Map.of("email", email, "password", "wrong-password");
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Generates a unique email per test — prevents cross-test email conflicts. */
    private String uniqueEmail() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@clinicos.test";
    }
}
