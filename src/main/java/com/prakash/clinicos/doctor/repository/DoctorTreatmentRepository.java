package com.prakash.clinicos.doctor.repository;

import com.prakash.clinicos.doctor.entity.DoctorTreatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DoctorTreatmentRepository extends JpaRepository<DoctorTreatment, Long> {

    List<DoctorTreatment> findByDoctorId(Long doctorId);

    List<DoctorTreatment> findByDoctorIdAndActiveTrue(Long doctorId);

    Optional<DoctorTreatment> findByDoctorIdAndTreatmentTypeId(Long doctorId, Long treatmentTypeId);

    boolean existsByDoctorIdAndTreatmentTypeId(Long doctorId, Long treatmentTypeId);

    @Transactional
    void deleteByDoctorIdAndTreatmentTypeId(Long doctorId, Long treatmentTypeId);
}
