package com.prakash.clinicos.medical.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prescription_medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionMedicine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prescription_id", nullable = false)
    private Long prescriptionId;

    @Column(name = "medicine_name", nullable = false, length = 255)
    private String medicineName;

    /** e.g. "5mg", "10ml", "1 tablet" */
    @Column(nullable = false, length = 100)
    private String dosage;

    /** e.g. "Once daily after meals", "Twice daily" */
    @Column(nullable = false, length = 100)
    private String frequency;

    @Column(name = "duration_days")
    private Integer durationDays;

    /** e.g. "Oral", "Topical", "IV", "Sublingual" */
    @Column(length = 50)
    private String route;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
