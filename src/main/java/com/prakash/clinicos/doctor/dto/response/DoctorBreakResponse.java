package com.prakash.clinicos.doctor.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Builder
public class DoctorBreakResponse {

    private Long id;
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime breakStart;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime breakEnd;

    private String breakType;
    private String label;
}
