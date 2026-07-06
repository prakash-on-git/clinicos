package com.prakash.clinicos.medical.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class CreatePrescriptionRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    /** Optional — links prescription to a formal appointment. */
    private Long appointmentId;

    private String diagnosis;
    private String instructions;
    private LocalDate followUpDate;

    @Valid
    private List<PrescriptionMedicineRequest> medicines;

    public Long getPatientId()           { return patientId; }
    public Long getDoctorId()            { return doctorId; }
    public Long getAppointmentId()       { return appointmentId; }
    public String getDiagnosis()         { return diagnosis; }
    public String getInstructions()      { return instructions; }
    public LocalDate getFollowUpDate()   { return followUpDate; }
    public List<PrescriptionMedicineRequest> getMedicines() { return medicines; }
}
