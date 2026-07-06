package com.prakash.clinicos.medical.dto.request;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

/**
 * All fields are optional — only provided fields are applied.
 * Sending an empty medicines list clears all existing medicines.
 * Omitting medicines (null) leaves existing medicines unchanged.
 */
public class UpdatePrescriptionRequest {

    private String diagnosis;
    private String instructions;
    private LocalDate followUpDate;

    @Valid
    private List<PrescriptionMedicineRequest> medicines;

    public String getDiagnosis()       { return diagnosis; }
    public String getInstructions()    { return instructions; }
    public LocalDate getFollowUpDate() { return followUpDate; }
    public List<PrescriptionMedicineRequest> getMedicines() { return medicines; }
}
