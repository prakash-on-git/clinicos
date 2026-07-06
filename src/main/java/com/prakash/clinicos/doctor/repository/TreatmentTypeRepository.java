package com.prakash.clinicos.doctor.repository;

import com.prakash.clinicos.doctor.entity.TreatmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TreatmentTypeRepository extends JpaRepository<TreatmentType, Long> {

    Optional<TreatmentType> findByIdAndDeletedFalse(Long id);

    Optional<TreatmentType> findByIdAndClinicIdAndDeletedFalse(Long id, Long clinicId);

    boolean existsByClinicIdAndNameAndDeletedFalse(Long clinicId, String name);

    List<TreatmentType> findByClinicIdAndDeletedFalseOrderByNameAsc(Long clinicId);

    List<TreatmentType> findByClinicIdAndDeletedFalseAndActiveOrderByNameAsc(Long clinicId, boolean active);
}
