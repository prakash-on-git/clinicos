package com.prakash.clinicos.medical.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrescriptionResponse {

    private Long id;
    private Long clinicId;
    private Long doctorId;
    private Long patientId;
    private Long appointmentId;

    private String doctorName;
    private String patientName;

    private String diagnosis;
    private String instructions;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate followUpDate;

    private List<PrescriptionMedicineResponse> medicines;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
