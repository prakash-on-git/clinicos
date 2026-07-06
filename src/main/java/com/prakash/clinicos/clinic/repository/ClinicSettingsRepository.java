package com.prakash.clinicos.clinic.repository;

import com.prakash.clinicos.clinic.entity.ClinicSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClinicSettingsRepository extends JpaRepository<ClinicSettings, Long> {

    Optional<ClinicSettings> findByClinicId(Long clinicId);
}
