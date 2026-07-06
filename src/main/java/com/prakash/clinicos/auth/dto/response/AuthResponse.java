package com.prakash.clinicos.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

/**
 * Returned on: register, login, refresh.
 *
 * @JsonInclude(NON_NULL) → fields that are null are omitted from the JSON output.
 * Example: emailVerificationToken is only present after registration,
 * so login and refresh responses won't include it at all.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;       // always "Bearer"
    private Long expiresIn;         // access token lifetime in seconds (for the client to track)
    private Long userId;
    private String email;
    private String fullName;
    private Set<String> roles;

    /**
     * DEV MODE ONLY – returned after registration so you can test /verify-email
     * without an email server. In production this would be sent via email and
     * NOT included in the API response.
     */
    private String emailVerificationToken;
}
