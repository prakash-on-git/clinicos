package com.prakash.clinicos.auth.service;

import com.prakash.clinicos.auth.dto.request.*;
import com.prakash.clinicos.auth.dto.response.AuthResponse;
import com.prakash.clinicos.auth.dto.response.ForgotPasswordResponse;
import com.prakash.clinicos.auth.dto.response.MessageResponse;
import com.prakash.clinicos.auth.entity.RefreshToken;
import com.prakash.clinicos.auth.entity.Role;
import com.prakash.clinicos.auth.entity.User;
import com.prakash.clinicos.auth.repository.RefreshTokenRepository;
import com.prakash.clinicos.auth.repository.RoleRepository;
import com.prakash.clinicos.auth.repository.UserRepository;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.security.JwtTokenProvider;
import com.prakash.clinicos.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService — no Spring context, no DB, pure Mockito.
 *
 * Strategy: mock every dependency; test only the business logic in AuthService.
 * Each test exercises one specific scenario (happy path or specific error).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private AuthenticationManager authManager;

    @InjectMocks
    private AuthService authService;

    /** Inject @Value field that @InjectMocks can't populate (no Spring context). */
    @BeforeEach
    void injectValueFields() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L); // 7 days
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    void register_newEmail_returnsAuthResponseWithTokens() {
        // Arrange
        Role role = Role.builder().id(1L).name("CLINIC_ADMIN").build();
        User savedUser = User.builder()
                .id(1L).fullName("Test Admin").email("admin@test.com")
                .password("hashed-pw").roles(Set.of(role)).build();
        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-uuid").user(savedUser)
                .expiresAt(LocalDateTime.now().plusDays(7)).build();

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(roleRepository.findByName("CLINIC_ADMIN")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("hashed-pw");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-jwt");
        when(tokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test Admin");
        req.setEmail("admin@test.com");
        req.setPassword("password123");

        // Act
        AuthResponse response = authService.register(req);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo("access-jwt");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-uuid");
        assertThat(response.getEmail()).isEqualTo("admin@test.com");
        assertThat(response.getRoles()).contains("CLINIC_ADMIN");
        assertThat(response.getEmailVerificationToken()).isNotNull(); // included in dev mode
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        // Arrange
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test");
        req.setEmail("existing@test.com");
        req.setPassword("password123");

        // Act + Assert
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("already registered")
                .extracting(e -> ((AppException) e).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsTokens() {
        // Arrange
        Role role = Role.builder().id(1L).name("CLINIC_ADMIN").build();
        User user = User.builder()
                .id(1L).email("admin@test.com").password("hashed")
                .roles(Set.of(role)).build();
        UserPrincipal principal = UserPrincipal.from(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        when(authManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        RefreshToken refreshToken = RefreshToken.builder()
                .token("new-refresh").user(user)
                .expiresAt(LocalDateTime.now().plusDays(7)).build();
        when(tokenProvider.generateAccessToken(any())).thenReturn("new-access");
        when(tokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(refreshTokenRepository.save(any())).thenReturn(refreshToken);

        LoginRequest req = new LoginRequest();
        req.setEmail("admin@test.com");
        req.setPassword("password123");

        // Act
        AuthResponse response = authService.login(req);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getEmail()).isEqualTo("admin@test.com");
        verify(refreshTokenRepository).deleteByUser(user); // old sessions cleared
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    void logout_validToken_deletesRefreshToken() {
        // Arrange
        LogoutRequest req = mock(LogoutRequest.class);
        when(req.getRefreshToken()).thenReturn("some-token");

        // Act
        MessageResponse response = authService.logout(req);

        // Assert
        verify(refreshTokenRepository).deleteByToken("some-token");
        assertThat(response.getMessage()).contains("Logged out");
    }

    // ── forgotPassword ────────────────────────────────────────────────────────

    @Test
    void forgotPassword_emailNotFound_returnsGenericMessageWithoutToken() {
        // Arrange — security: don't reveal if email exists
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        ForgotPasswordRequest req = mock(ForgotPasswordRequest.class);
        when(req.getEmail()).thenReturn("ghost@test.com");

        // Act
        ForgotPasswordResponse response = authService.forgotPassword(req);

        // Assert
        assertThat(response.getResetToken()).isNull();
        assertThat(response.getMessage()).contains("If this email is registered");
        verify(userRepository, never()).save(any());
    }

    @Test
    void forgotPassword_emailFound_setsResetTokenAndReturnsIt() {
        // Arrange
        User user = User.builder().id(1L).email("real@test.com").password("hashed").build();
        when(userRepository.findByEmail("real@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        ForgotPasswordRequest req = mock(ForgotPasswordRequest.class);
        when(req.getEmail()).thenReturn("real@test.com");

        // Act
        ForgotPasswordResponse response = authService.forgotPassword(req);

        // Assert
        assertThat(response.getResetToken()).isNotNull();
        assertThat(user.getPasswordResetToken()).isNotNull();
        assertThat(user.getPasswordResetTokenExpiresAt()).isAfter(LocalDateTime.now());
    }

    // ── resetPassword ─────────────────────────────────────────────────────────

    @Test
    void resetPassword_expiredToken_throwsBadRequest() {
        // Arrange
        User user = User.builder()
                .id(1L).email("user@test.com").password("old-hash")
                .passwordResetToken("expired-token")
                .passwordResetTokenExpiresAt(LocalDateTime.now().minusHours(2)) // expired
                .build();
        when(userRepository.findByPasswordResetToken("expired-token")).thenReturn(Optional.of(user));

        ResetPasswordRequest req = mock(ResetPasswordRequest.class);
        when(req.getResetToken()).thenReturn("expired-token");

        // Act + Assert
        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("expired")
                .extracting(e -> ((AppException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── verifyEmail ───────────────────────────────────────────────────────────

    @Test
    void verifyEmail_alreadyVerified_throwsConflict() {
        // Arrange
        User user = User.builder()
                .id(1L).email("user@test.com").password("hashed")
                .emailVerifyToken("verify-token")
                .emailVerified(true) // already done
                .build();
        when(userRepository.findByEmailVerifyToken("verify-token")).thenReturn(Optional.of(user));

        VerifyEmailRequest req = mock(VerifyEmailRequest.class);
        when(req.getToken()).thenReturn("verify-token");

        // Act + Assert
        assertThatThrownBy(() -> authService.verifyEmail(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("already verified")
                .extracting(e -> ((AppException) e).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void verifyEmail_validToken_setsVerifiedAndClearsToken() {
        // Arrange
        User user = User.builder()
                .id(1L).email("user@test.com").password("hashed")
                .emailVerifyToken("verify-token")
                .emailVerified(false)
                .build();
        when(userRepository.findByEmailVerifyToken("verify-token")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        VerifyEmailRequest req = mock(VerifyEmailRequest.class);
        when(req.getToken()).thenReturn("verify-token");

        // Act
        MessageResponse response = authService.verifyEmail(req);

        // Assert
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getEmailVerifyToken()).isNull(); // token is single-use — cleared
        assertThat(response.getMessage()).contains("verified");
    }
}
