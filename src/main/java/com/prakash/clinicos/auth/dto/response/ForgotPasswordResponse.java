package com.prakash.clinicos.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Returned by POST /api/v1/auth/forgot-password.
 *
 * resetToken is returned directly in DEV MODE ONLY.
 * In production, it would be emailed as a link and never included here.
 */
@Getter
@AllArgsConstructor
public class ForgotPasswordResponse {
    private String message;
    private String resetToken; // DEV MODE – remove in production
}
