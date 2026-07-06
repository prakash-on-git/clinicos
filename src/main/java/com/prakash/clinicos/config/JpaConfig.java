package com.prakash.clinicos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activates JPA Auditing.
 *
 * Without @EnableJpaAuditing, the @CreatedDate and @LastModifiedDate
 * annotations in BaseEntity do nothing — the fields stay null forever.
 * This single annotation turns on the auditing infrastructure.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
