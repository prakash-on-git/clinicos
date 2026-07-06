package com.prakash.clinicos.medical.repository;

import com.prakash.clinicos.medical.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findByIdAndClinicId(Long id, Long clinicId);

    Optional<Prescription> findByAppointmentIdAndClinicId(Long appointmentId, Long clinicId);

    Page<Prescription> findByPatientIdAndClinicIdOrderByCreatedAtDesc(
            Long patientId, Long clinicId, Pageable pageable);

    boolean existsByAppointmentId(Long appointmentId);
}
