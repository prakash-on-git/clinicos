package com.prakash.clinicos.auth.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Stored as a bcrypt hash. Never stored or logged in plain text.
     * BCryptPasswordEncoder (configured in SecurityConfig) handles hashing.
     */
    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    /** UUID token sent via email. Cleared after verification. */
    @Column(name = "email_verify_token", length = 255)
    private String emailVerifyToken;

    /** UUID token for password reset. Cleared after use. */
    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;

    /** Expiry time for the reset token (1 hour from generation). */
    @Column(name = "password_reset_token_expires_at")
    private LocalDateTime passwordResetTokenExpiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /**
     * The clinic this CLINIC_ADMIN owns. Null until they create a clinic.
     * Stored as a plain FK (not @ManyToOne) to avoid cross-module entity coupling.
     * Set automatically when the CLINIC_ADMIN calls POST /api/v1/clinics.
     */
    @Column(name = "clinic_id")
    private Long clinicId;

    /**
     * FetchType.EAGER: roles are always needed when we load a User for security.
     * LAZY would cause LazyInitializationException outside a transaction.
     * For a small set like roles, EAGER is acceptable.
     *
     * The join table column names MUST exactly match V2__auth_schema.sql.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
