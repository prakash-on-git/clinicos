package com.prakash.clinicos.auth.repository;

import com.prakash.clinicos.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA generates the SQL for all these methods automatically
 * by parsing the method names. You never write the implementation.
 *
 * findByEmail            → SELECT * FROM users WHERE email = ?
 * existsByEmail          → SELECT COUNT(*) > 0 FROM users WHERE email = ?
 * findByEmailVerifyToken → SELECT * FROM users WHERE email_verify_token = ?
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailVerifyToken(String token);

    Optional<User> findByPasswordResetToken(String token);
}
