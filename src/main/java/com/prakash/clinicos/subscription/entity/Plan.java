package com.prakash.clinicos.subscription.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private PlanTier tier;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "max_doctors")
    private Integer maxDoctors;  // null = unlimited

    @Column(name = "max_patients_per_month")
    private Integer maxPatientsPerMonth;  // null = unlimited

    @Column(name = "price_monthly", nullable = false)
    private BigDecimal priceMonthly;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
