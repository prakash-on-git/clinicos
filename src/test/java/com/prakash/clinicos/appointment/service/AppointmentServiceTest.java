package com.prakash.clinicos.appointment.service;

import com.prakash.clinicos.appointment.dto.request.CancelAppointmentRequest;
import com.prakash.clinicos.appointment.dto.request.UpdateAppointmentStatusRequest;
import com.prakash.clinicos.appointment.entity.Appointment;
import com.prakash.clinicos.appointment.entity.AppointmentStatus;
import com.prakash.clinicos.appointment.repository.AppointmentRepository;
import com.prakash.clinicos.audit.service.AuditService;
import com.prakash.clinicos.clinic.entity.Clinic;
import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.doctor.entity.Doctor;
import com.prakash.clinicos.doctor.repository.DoctorRepository;
import com.prakash.clinicos.doctor.repository.DoctorTreatmentRepository;
import com.prakash.clinicos.doctor.repository.TreatmentTypeRepository;
import com.prakash.clinicos.doctor.service.DoctorAvailabilityService;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.notification.service.NotificationService;
import com.prakash.clinicos.patient.entity.Patient;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AppointmentService — pure Mockito, no Spring context, no DB.
 *
 * Focus: validate the 8-step booking validation, status lifecycle guards,
 * and terminal status enforcement. These are the most critical business rules
 * in the system and the hardest to catch at the integration level.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private ClinicRepository clinicRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private TreatmentTypeRepository treatmentTypeRepository;
    @Mock private DoctorTreatmentRepository doctorTreatmentRepository;
    @Mock private DoctorAvailabilityService availabilityService;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;

    @InjectMocks
    private AppointmentService appointmentService;

    // ── bookAppointment: validation step 1 — clinic must exist ────────────────

    @Test
    void bookAppointment_clinicNotFound_throwsNotFound() {
        when(clinicRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        var req = mock(com.prakash.clinicos.appointment.dto.request.BookAppointmentRequest.class);
        var principal = mock(UserPrincipal.class);

        assertThatThrownBy(() -> appointmentService.bookAppointment(99L, req, principal))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Clinic not found")
                .extracting(e -> ((AppException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── bookAppointment: validation step 2 — doctor must belong to clinic ─────

    @Test
    void bookAppointment_doctorBelongsToDifferentClinic_throwsBadRequest() {
        // Clinic 1 exists
        when(clinicRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(new Clinic()));

        // Doctor 10 belongs to clinic 99 (not 1)
        Doctor doctor = Doctor.builder().id(10L).clinicId(99L).active(true).fullName("Dr. Wrong").build();
        when(doctorRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(doctor));

        var req = mock(com.prakash.clinicos.appointment.dto.request.BookAppointmentRequest.class);
        when(req.getDoctorId()).thenReturn(10L);
        var principal = mock(UserPrincipal.class);

        assertThatThrownBy(() -> appointmentService.bookAppointment(1L, req, principal))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("does not belong to this clinic")
                .extracting(e -> ((AppException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── bookAppointment: validation step 2 — doctor must be active ────────────

    @Test
    void bookAppointment_inactiveDoctor_throwsBadRequest() {
        when(clinicRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(new Clinic()));

        // Doctor belongs to clinic but is inactive
        Doctor doctor = Doctor.builder().id(10L).clinicId(1L).active(false).fullName("Dr. Inactive").build();
        when(doctorRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(doctor));

        var req = mock(com.prakash.clinicos.appointment.dto.request.BookAppointmentRequest.class);
        when(req.getDoctorId()).thenReturn(10L);
        var principal = mock(UserPrincipal.class);

        assertThatThrownBy(() -> appointmentService.bookAppointment(1L, req, principal))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("not currently active")
                .extracting(e -> ((AppException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── cancelAppointment: terminal status guard ───────────────────────────────

    @Test
    void cancelAppointment_alreadyCancelled_throwsConflict() {
        // Appointment is already in a terminal status
        Appointment appt = Appointment.builder()
                .id(5L).clinicId(1L).doctorId(10L).patientId(20L)
                .status(AppointmentStatus.CANCELLED)
                .appointmentDate(LocalDate.now())
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 10))
                .durationMins(10).build();

        when(appointmentRepository.findByIdAndClinicId(5L, 1L)).thenReturn(Optional.of(appt));

        // assertNotTerminal() throws before req.getReason() is ever called — don't stub it
        var req = mock(CancelAppointmentRequest.class);
        var principal = mock(UserPrincipal.class);

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, 5L, req, principal))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("already cancelled")
                .extracting(e -> ((AppException) e).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    // ── updateStatus: invalid lifecycle transition ─────────────────────────────

    @Test
    void updateStatus_pendingToCompleted_throwsConflict() {
        /*
         * Valid transitions from PENDING: only → CONFIRMED
         * Jumping directly to COMPLETED is invalid.
         * This validates that the status machine is enforced.
         */
        Appointment appt = Appointment.builder()
                .id(5L).clinicId(1L).doctorId(10L).patientId(20L)
                .status(AppointmentStatus.PENDING)
                .appointmentDate(LocalDate.now())
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 10))
                .durationMins(10).build();

        when(appointmentRepository.findByIdAndClinicId(5L, 1L)).thenReturn(Optional.of(appt));

        var req = mock(UpdateAppointmentStatusRequest.class);
        when(req.getStatus()).thenReturn(AppointmentStatus.COMPLETED); // invalid: PENDING → COMPLETED
        var principal = mock(UserPrincipal.class);

        assertThatThrownBy(() -> appointmentService.updateStatus(1L, 5L, req, principal))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Cannot transition")
                .extracting(e -> ((AppException) e).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    // ── updateStatus: valid transition ────────────────────────────────────────

    @Test
    void updateStatus_pendingToConfirmed_returnsConfirmedAppointment() {
        Doctor doctor = Doctor.builder().id(10L).clinicId(1L).fullName("Dr. Test").active(true).build();
        Patient patient = Patient.builder().id(20L).clinicId(1L).firstName("Jane").build();

        Appointment appt = Appointment.builder()
                .id(5L).clinicId(1L).doctorId(10L).patientId(20L)
                .status(AppointmentStatus.PENDING)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 10))
                .durationMins(10).build();

        Appointment confirmed = Appointment.builder()
                .id(5L).clinicId(1L).doctorId(10L).patientId(20L)
                .status(AppointmentStatus.CONFIRMED)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 10))
                .durationMins(10).build();

        when(appointmentRepository.findByIdAndClinicId(5L, 1L)).thenReturn(Optional.of(appt));
        when(appointmentRepository.save(any())).thenReturn(confirmed);
        when(doctorRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findByIdAndDeletedFalse(20L)).thenReturn(Optional.of(patient));

        var req = mock(UpdateAppointmentStatusRequest.class);
        when(req.getStatus()).thenReturn(AppointmentStatus.CONFIRMED);
        when(req.getNotes()).thenReturn(null);

        var principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(1L);

        var result = appointmentService.updateStatus(1L, 5L, req, principal);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(result.getDoctorName()).isEqualTo("Dr. Test");
        verify(appointmentRepository).save(any());
    }
}
