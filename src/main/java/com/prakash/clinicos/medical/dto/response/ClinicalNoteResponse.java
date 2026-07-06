package com.prakash.clinicos.medical.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClinicalNoteResponse {

    private Long id;
    private Long clinicId;
    private Long doctorId;
    private Long patientId;
    private Long appointmentId;

    private String doctorName;
    private String patientName;

    private String subjective;
    private String objective;
    private String assessment;
    private String plan;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
