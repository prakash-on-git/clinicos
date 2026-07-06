package com.prakash.clinicos.auth.repository;

import com.prakash.clinicos.auth.entity.RefreshToken;
import com.prakash.clinicos.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * Delete all active sessions for a user.
     * Used on: password reset (forces re-login on all devices for security).
     *
     * @Transactional required on delete-by derived queries.
     */
    @Transactional
    void deleteByUser(User user);

    /**
     * Delete a specific session by token.
     * Used on: logout (invalidate just this device's session).
     */
    @Transactional
    void deleteByToken(String token);
}
