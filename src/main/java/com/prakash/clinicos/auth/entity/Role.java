package com.prakash.clinicos.auth.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Stored as plain name: SUPER_ADMIN, CLINIC_ADMIN, DOCTOR, RECEPTIONIST.
     * Spring Security expects authority strings. When we build UserPrincipal,
     * we prefix with "ROLE_" so hasRole("CLINIC_ADMIN") works in security expressions.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
