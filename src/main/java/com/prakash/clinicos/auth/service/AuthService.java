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
import com.prakash.clinicos.patient.entity.Patient;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.security.JwtTokenProvider;
import com.prakash.clinicos.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * All authentication business logic lives here. Controllers stay thin.
 *
 * @Transactional on each method ensures that if anything fails mid-method,
 * all DB changes are rolled back automatically. No partial writes.
 */
@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authManager;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PatientRepository patientRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuthenticationManager authManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authManager = authManager;
    }

    // ── Register ─────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new AppException(HttpStatus.CONFLICT, "Email is already registered");
        }

        // Default role for self-registration: CLINIC_ADMIN
        // Seeded in V2__auth_schema.sql — always exists
        Role defaultRole = roleRepository.findByName("CLINIC_ADMIN")
                .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Default role not found. Check if V2 migration ran correctly."));

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .fullName(req.getFullName())
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .emailVerifyToken(verificationToken)
                .roles(Set.of(defaultRole))
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", email);

        UserPrincipal principal = UserPrincipal.from(user);
        String accessToken = tokenProvider.generateAccessToken(principal);
        RefreshToken refreshToken = saveRefreshToken(user);

        // emailVerificationToken included in response for dev/testing.
        // In production: send via email, remove from response.
        return buildAuthResponse(accessToken, refreshToken.getToken(), user, verificationToken);
    }

    // ── Patient self-registration ─────────────────────────────────────────────

    /**
     * Creates a login account for an existing patient and links it.
     *
     * The patient must already be in the system (registered by clinic staff).
     * We look them up by phone + clinicId, create a User with PATIENT role,
     * and write user.id back into patient.userId so the link is bidirectional.
     */
    @Transactional
    public AuthResponse registerPatient(PatientRegisterRequest req) {
        String email = req.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new AppException(HttpStatus.CONFLICT, "Email is already registered");
        }

        Patient patient = patientRepository
                .findByPhoneAndClinicIdAndDeletedFalse(req.getPhone(), req.getClinicId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "No patient found with that phone number in this clinic"));

        if (patient.getUserId() != null) {
            throw new AppException(HttpStatus.CONFLICT,
                    "A portal account already exists for this patient");
        }

        Role patientRole = roleRepository.findByName("PATIENT")
                .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "PATIENT role not found. Check if V11 migration ran correctly."));

        String fullName = patient.getLastName() != null
                ? patient.getFirstName() + " " + patient.getLastName()
                : patient.getFirstName();

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .emailVerified(true)   // patient identity already verified by clinic staff
                .roles(Set.of(patientRole))
                .build();

        user = userRepository.save(user);

        // Link back to patient record
        patient.setUserId(user.getId());
        patientRepository.save(patient);

        log.info("Patient portal account created: userId={}, patientId={}", user.getId(), patient.getId());

        UserPrincipal principal = UserPrincipal.from(user);
        String accessToken = tokenProvider.generateAccessToken(principal);
        RefreshToken refreshToken = saveRefreshToken(user);

        return buildAuthResponse(accessToken, refreshToken.getToken(), user, null);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest req) {
        /*
         * authManager.authenticate() does two things:
         *   1. Calls CustomUserDetailsService.loadUserByUsername(email) to fetch the user
         *   2. Uses BCryptPasswordEncoder to compare the provided password with the hash
         * If credentials are wrong, it throws BadCredentialsException
         * which GlobalExceptionHandler converts to 401.
         */
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getEmail().toLowerCase().trim(),
                        req.getPassword()
                )
        );

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        // Delete all previous sessions for this user (single active session policy)
        // Remove this line if you want multi-device login
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));

        refreshTokenRepository.deleteByUser(user);

        String accessToken = tokenProvider.generateAccessToken(principal);
        RefreshToken refreshToken = saveRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(accessToken, refreshToken.getToken(), user, null);
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest req) {
        RefreshToken stored = refreshTokenRepository.findByToken(req.getRefreshToken())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token. Please log in again."));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "Refresh token has expired. Please log in again.");
        }

        /*
         * Token Rotation: delete the used token, issue a new one.
         * Why? If someone steals your refresh token and uses it,
         * the next time you try to refresh, YOUR token won't exist
         * → you'll be logged out and know something is wrong.
         * Without rotation, a stolen token stays valid for 7 days.
         */
        User user = stored.getUser();
        refreshTokenRepository.delete(stored);

        UserPrincipal principal = UserPrincipal.from(user);
        String newAccessToken = tokenProvider.generateAccessToken(principal);
        RefreshToken newRefreshToken = saveRefreshToken(user);

        return buildAuthResponse(newAccessToken, newRefreshToken.getToken(), user, null);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Transactional
    public MessageResponse logout(LogoutRequest req) {
        // Delete only this specific session (allows multi-device login)
        refreshTokenRepository.deleteByToken(req.getRefreshToken());
        return new MessageResponse("Logged out successfully");
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest req) {
        String email = req.getEmail().toLowerCase().trim();

        /*
         * Security note: don't reveal whether the email exists.
         * Return the same success message regardless so attackers
         * can't use this endpoint to discover which emails are registered.
         */
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            log.info("Password reset requested for: {}", email);

            // DEV: return token directly. PROD: email the link, return generic message.
            return new ForgotPasswordResponse(
                    "Password reset link sent (dev mode: token returned for testing)",
                    resetToken
            );
        }

        return new ForgotPasswordResponse(
                "If this email is registered, a reset link has been sent.", null);
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByPasswordResetToken(req.getResetToken())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                        "Invalid or expired reset token"));

        if (user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);

        // Force re-login on all devices after password change
        refreshTokenRepository.deleteByUser(user);

        log.info("Password reset successful for: {}", user.getEmail());
        return new MessageResponse("Password reset successfully. Please log in with your new password.");
    }

    // ── Verify Email ──────────────────────────────────────────────────────────

    @Transactional
    public MessageResponse verifyEmail(VerifyEmailRequest req) {
        User user = userRepository.findByEmailVerifyToken(req.getToken())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                        "Invalid or already used verification token"));

        if (user.isEmailVerified()) {
            throw new AppException(HttpStatus.CONFLICT, "Email is already verified");
        }

        user.setEmailVerified(true);
        user.setEmailVerifyToken(null); // token is single-use — clear it
        userRepository.save(user);

        return new MessageResponse("Email verified successfully. You can now log in.");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private RefreshToken saveRefreshToken(User user) {
        long expirationSeconds = refreshTokenExpirationMs / 1000;
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(expirationSeconds))
                .build();
        return refreshTokenRepository.save(token);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken,
                                           User user, String emailVerificationToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpirationMs() / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .emailVerificationToken(emailVerificationToken) // null on login/refresh
                .build();
    }
}
