package com.prakash.clinicos.ai.tool;

import com.prakash.clinicos.appointment.dto.response.AppointmentResponse;
import com.prakash.clinicos.appointment.service.AppointmentService;
import com.prakash.clinicos.security.UserPrincipal;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/** Looks up a patient's appointment history — past visits and upcoming bookings. */
@Component
public class PatientHistoryTool implements ClinicalTool {

    private final AppointmentService appointmentService;
    private final ObjectMapper objectMapper;

    public PatientHistoryTool(AppointmentService appointmentService, ObjectMapper objectMapper) {
        this.appointmentService = appointmentService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "get_patient_appointment_history";
    }

    @Override
    public String description() {
        return "Get a patient's appointment history at this clinic (past and upcoming), "
                + "including doctor, date, time, and status. Requires a patientId from search_patients.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return JsonSchema.object(
                JsonSchema.properties("patientId", JsonSchema.integer("The patient's ID")),
                List.of("patientId"));
    }

    @Override
    public Object execute(Long clinicId, UserPrincipal principal, String argumentsJson) {
        JsonNode args = objectMapper.readTree(argumentsJson);
        long patientId = args.path("patientId").asLong();

        List<AppointmentResponse> history = appointmentService.getPatientHistory(clinicId, patientId);
        List<Visit> visits = history.stream()
                .map(a -> new Visit(a.getId(), a.getDoctorName(), a.getAppointmentDate(),
                        a.getStartTime(), a.getStatus().name(), a.getReason()))
                .toList();

        return visits.isEmpty() ? Map.of("message", "No appointment history for this patient") : visits;
    }

    private record Visit(Long appointmentId, String doctorName, java.time.LocalDate date,
                          java.time.LocalTime startTime, String status, String reason) {}
}
