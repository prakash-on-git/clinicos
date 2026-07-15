package com.prakash.clinicos.clinic;

import com.prakash.clinicos.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Clinics API.
 *
 * Covers:
 *  - Public read access (GET /clinics)
 *  - Auth enforcement (POST without token is rejected)
 *  - Successful create with CLINIC_ADMIN JWT
 *  - Duplicate clinic prevention (one admin = one clinic)
 */
@Transactional
class ClinicIntegrationTest extends AbstractIntegrationTest {

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL    = "/api/v1/auth/login";
    private static final String CLINICS_URL  = "/api/v1/clinics";

    // ── Public access ─────────────────────────────────────────────────────────

    @Test
    void listClinics_noAuth_returns200() throws Exception {
        // GET /clinics is explicitly permitted in SecurityConfig — no token needed
        mockMvc.perform(get(CLINICS_URL))
                .andExpect(status().isOk());
    }

    // ── Auth enforcement ──────────────────────────────────────────────────────

    @Test
    void createClinic_noAuthToken_returns4xx() throws Exception {
        Map<String, String> body = Map.of("name", "No Auth Clinic");

        // Without a JWT, Spring Security rejects the request before it hits the controller
        mockMvc.perform(post(CLINICS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is4xxClientError());
    }

    // ── Successful create ─────────────────────────────────────────────────────

    @Test
    void createClinic_withValidAdminToken_returns201() throws Exception {
        String token = registerAndGetToken();

        Map<String, String> clinicBody = Map.of(
                "name",     "City Health Clinic",
                "phone",    "+91 98765 43210",
                "city",     "Mumbai",
                "timezone", "Asia/Kolkata"
        );

        mockMvc.perform(post(CLINICS_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clinicBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("City Health Clinic"))
                .andExpect(jsonPath("$.city").value("Mumbai"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    // ── Duplicate clinic prevention ───────────────────────────────────────────

    @Test
    void createClinic_adminAlreadyOwnsOne_returns409() throws Exception {
        String token = registerAndGetToken();
        Map<String, String> clinicBody = Map.of("name", "First Clinic");

        // First clinic created successfully
        mockMvc.perform(post(CLINICS_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clinicBody)))
                .andExpect(status().isCreated());

        // Second clinic by the same admin → 409
        Map<String, String> secondClinicBody = Map.of("name", "Second Clinic");
        mockMvc.perform(post(CLINICS_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondClinicBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.containsString("already own a clinic")));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Registers a new CLINIC_ADMIN and returns their JWT access token.
     * Each call creates a unique user so tests don't interfere with each other.
     */
    @SuppressWarnings("unchecked")
    private String registerAndGetToken() throws Exception {
        String email = "admin-" + UUID.randomUUID().toString().substring(0, 8) + "@clinicos.test";

        Map<String, String> regBody = Map.of(
                "fullName", "Test Admin",
                "email",    email,
                "password", "password123"
        );

        MvcResult result = mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regBody)))
                .andExpect(status().isCreated())
                .andReturn();

        // Parse the accessToken from the JSON response
        Map<String, Object> response = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class);
        return (String) response.get("accessToken");
    }
}
