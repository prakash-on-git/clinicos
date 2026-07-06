package com.prakash.clinicos.config;

import com.prakash.clinicos.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures Spring Security.
 *
 * Key decisions explained:
 *
 * 1. No WebSecurityConfigurerAdapter
 *    Removed in Spring Security 6. We declare @Bean methods instead.
 *
 * 2. csrf disabled
 *    CSRF attacks only apply to browser-based session cookies.
 *    Our API is stateless (JWT in Authorization header), so CSRF is irrelevant.
 *
 * 3. SessionCreationPolicy.STATELESS
 *    The server never creates or stores session state. Every request must
 *    carry a valid JWT. This enables horizontal scaling — any server instance
 *    can handle any request.
 *
 * 4. @EnableMethodSecurity
 *    Enables @PreAuthorize("hasRole('ADMIN')") annotations on controller methods.
 *    Used later when we add role-based access to clinic/doctor/patient endpoints.
 *
 * 5. No circular dependency
 *    SecurityConfig → JwtAuthenticationFilter → CustomUserDetailsService → UserRepository
 *    AuthService → AuthenticationManager (from AuthenticationConfiguration, not SecurityConfig)
 *    These two chains never form a cycle.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI — always public so reviewers/interviewers can explore the API
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/api-docs/**", "/api-docs").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()          // all auth endpoints are public
                        .requestMatchers("/api/v1/me/**").authenticated()        // patient portal — any authenticated user
                        // Patient data is private — must be authenticated even for reads.
                        // This rule must come BEFORE the broad clinic wildcard below.
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/v1/clinics/*/patients",
                                "/api/v1/clinics/*/patients/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/v1/clinics",
                                "/api/v1/clinics/**").permitAll()                // public clinic/doctor read access
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/v1/plans").permitAll()                     // public plan listing
                        .anyRequest().authenticated())                            // all writes require JWT
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * BCryptPasswordEncoder: industry-standard password hashing.
     *
     * Why not MD5 or SHA256?
     * MD5/SHA are fast — an attacker can hash millions of guesses per second.
     * BCrypt is intentionally slow (cost factor 10 by default = ~100ms per hash),
     * making brute force impractical. It also salts automatically.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes AuthenticationManager as a bean so AuthService can inject it.
     * AuthService calls authManager.authenticate() to validate login credentials.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
