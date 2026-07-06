package com.prakash.clinicos.auth.controller;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.prakash.clinicos.auth.dto.request.*;
import com.prakash.clinicos.auth.dto.response.AuthResponse;
import com.prakash.clinicos.auth.dto.response.ForgotPasswordResponse;
import com.prakash.clinicos.auth.dto.response.MessageResponse;
import com.prakash.clinicos.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Auth endpoints are all public (no JWT required).
 * Configured in SecurityConfig: .requestMatchers("/api/v1/auth/**").permitAll()
 *
 * Controllers are intentionally thin:
 *   - @Valid triggers Bean Validation → failure goes to GlobalExceptionHandler
 *   - Delegate to AuthService for all logic
 *   - Return appropriate HTTP status codes
 */
@Tag(name = "Authentication", description = "Register, login, refresh tokens, logout, forgot/reset password, and email verification.")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/patient/register")
    public ResponseEntity<AuthResponse> registerPatient(@Valid @RequestBody PatientRegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerPatient(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody LogoutRequest req) {
        return ResponseEntity.ok(authService.logout(req));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req) {
        return ResponseEntity.ok(authService.forgotPassword(req));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req) {
        return ResponseEntity.ok(authService.resetPassword(req));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest req) {
        return ResponseEntity.ok(authService.verifyEmail(req));
    }
}
