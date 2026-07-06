package com.prakash.clinicos.medical.repository;

import com.prakash.clinicos.medical.entity.Vitals;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VitalsRepository extends JpaRepository<Vitals, Long> {

    Optional<Vitals> findByAppointmentIdAndClinicId(Long appointmentId, Long clinicId);

    Page<Vitals> findByPatientIdAndClinicIdOrderByRecordedAtDesc(
            Long patientId, Long clinicId, Pageable pageable);
}
