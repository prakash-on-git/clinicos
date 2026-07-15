package com.prakash.clinicos.ai.tool;

import com.prakash.clinicos.appointment.dto.request.BookAppointmentRequest;
import com.prakash.clinicos.appointment.dto.response.AppointmentResponse;
import com.prakash.clinicos.appointment.service.AppointmentService;
import com.prakash.clinicos.security.UserPrincipal;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * Books a real appointment. Tenant scoping (doctor/patient must belong to
 * clinicId) and slot validation are enforced by AppointmentService itself —
 * this tool doesn't duplicate that logic, just parses model arguments into
 * the same request type the REST endpoint uses.
 */
@Component
public class BookAppointmentTool implements ClinicalTool {

    private final AppointmentService appointmentService;
    private final ObjectMapper objectMapper;

    public BookAppointmentTool(AppointmentService appointmentService, ObjectMapper objectMapper) {
        this.appointmentService = appointmentService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "book_appointment";
    }

    @Override
    public String description() {
        return "Book an appointment for a patient with a doctor. startTime must be one of the "
                + "freeSlots returned by check_doctor_availability for that doctor and date — "
                + "always call check_doctor_availability first.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return JsonSchema.object(
                JsonSchema.properties(
                        "patientId", JsonSchema.integer("The patient's ID, from search_patients"),
                        "doctorId", JsonSchema.integer("The doctor's ID"),
                        "appointmentDate", JsonSchema.string("Date to book, format yyyy-MM-dd"),
                        "startTime", JsonSchema.string("Start time, format HH:mm, must be a free slot"),
                        "reason", JsonSchema.string("Patient's stated reason for the visit (optional)")),
                List.of("patientId", "doctorId", "appointmentDate", "startTime"));
    }

    @Override
    public Object execute(Long clinicId, UserPrincipal principal, String argumentsJson) {
        BookAppointmentRequest request = objectMapper.readValue(argumentsJson, BookAppointmentRequest.class);
        AppointmentResponse booked = appointmentService.bookAppointment(clinicId, request, principal);

        return Map.of(
                "appointmentId", booked.getId(),
                "doctorName", booked.getDoctorName(),
                "patientName", booked.getPatientName(),
                "date", booked.getAppointmentDate().toString(),
                "startTime", booked.getStartTime().toString(),
                "status", booked.getStatus().name());
    }
}
