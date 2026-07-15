package com.prakash.clinicos.ai.tool;

import com.prakash.clinicos.doctor.dto.response.DoctorAvailabilityResponse;
import com.prakash.clinicos.doctor.entity.Doctor;
import com.prakash.clinicos.doctor.repository.DoctorRepository;
import com.prakash.clinicos.doctor.service.DoctorAvailabilityService;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Checks a doctor's free slots on a date — the model should call this before book_appointment. */
@Component
public class CheckAvailabilityTool implements ClinicalTool {

    private final DoctorAvailabilityService availabilityService;
    private final DoctorRepository doctorRepository;
    private final ObjectMapper objectMapper;

    public CheckAvailabilityTool(DoctorAvailabilityService availabilityService,
                                  DoctorRepository doctorRepository, ObjectMapper objectMapper) {
        this.availabilityService = availabilityService;
        this.doctorRepository = doctorRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "check_doctor_availability";
    }

    @Override
    public String description() {
        return "Get a doctor's free 10-minute appointment slots on a given date. "
                + "Call this before book_appointment to pick a valid startTime.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return JsonSchema.object(
                JsonSchema.properties(
                        "doctorId", JsonSchema.integer("The doctor's ID"),
                        "date", JsonSchema.string("Date to check, format yyyy-MM-dd")),
                List.of("doctorId", "date"));
    }

    @Override
    public Object execute(Long clinicId, UserPrincipal principal, String argumentsJson) {
        JsonNode args = objectMapper.readTree(argumentsJson);
        long doctorId = args.path("doctorId").asLong();
        LocalDate date = LocalDate.parse(args.path("date").asString());

        // computeAvailability() itself doesn't check clinic ownership — it only takes
        // a doctorId — so this tool must verify tenant scope before calling it.
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(doctorId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Doctor not found: " + doctorId));
        if (!doctor.getClinicId().equals(clinicId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Doctor does not belong to this clinic");
        }

        DoctorAvailabilityResponse availability = availabilityService.computeAvailability(doctorId, date);
        return Map.of(
                "available", availability.isAvailable(),
                "unavailableReason", availability.getUnavailableReason() == null ? "" : availability.getUnavailableReason(),
                "freeSlots", availability.getSlots());
    }
}
