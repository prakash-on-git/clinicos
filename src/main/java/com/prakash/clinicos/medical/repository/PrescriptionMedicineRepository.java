package com.prakash.clinicos.medical.repository;

import com.prakash.clinicos.medical.entity.PrescriptionMedicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionMedicineRepository extends JpaRepository<PrescriptionMedicine, Long> {

    List<PrescriptionMedicine> findByPrescriptionIdOrderById(Long prescriptionId);

    void deleteByPrescriptionId(Long prescriptionId);
}
