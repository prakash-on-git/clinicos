package com.prakash.clinicos.medical.entity;

import com.prakash.clinicos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clinical_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    /** S — Patient's reported symptoms / chief complaint. */
    @Column(columnDefinition = "TEXT")
    private String subjective;

    /** O — Examination findings, test results, observations. */
    @Column(columnDefinition = "TEXT")
    private String objective;

    /** A — Diagnosis or differential diagnosis. */
    @Column(columnDefinition = "TEXT")
    private String assessment;

    /** P — Treatment plan, referrals, follow-up instructions. */
    @Column(columnDefinition = "TEXT")
    private String plan;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
