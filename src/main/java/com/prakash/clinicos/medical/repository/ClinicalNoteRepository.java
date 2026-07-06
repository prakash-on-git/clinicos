package com.prakash.clinicos.medical.repository;

import com.prakash.clinicos.medical.entity.ClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, Long> {

    Optional<ClinicalNote> findByAppointmentIdAndClinicId(Long appointmentId, Long clinicId);
}
