package com.prakash.clinicos.portal.service;

import com.prakash.clinicos.appointment.dto.response.AppointmentResponse;
import com.prakash.clinicos.appointment.entity.Appointment;
import com.prakash.clinicos.appointment.repository.AppointmentRepository;
import com.prakash.clinicos.billing.dto.response.InvoiceResponse;
import com.prakash.clinicos.billing.service.BillingService;
import com.prakash.clinicos.doctor.entity.Doctor;
import com.prakash.clinicos.doctor.repository.DoctorRepository;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.medical.dto.response.PrescriptionResponse;
import com.prakash.clinicos.medical.service.PrescriptionService;
import com.prakash.clinicos.patient.entity.Patient;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.portal.dto.request.PatientNotificationPreferencesRequest;
import com.prakash.clinicos.portal.dto.response.PatientProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientPortalService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PrescriptionService prescriptionService;
    private final BillingService billingService;

    public PatientPortalService(PatientRepository patientRepository,
                                 AppointmentRepository appointmentRepository,
                                 DoctorRepository doctorRepository,
                                 PrescriptionService prescriptionService,
                                 BillingService billingService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.prescriptionService = prescriptionService;
        this.billingService = billingService;
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PatientProfileResponse getMyProfile(Long userId) {
        return toProfileResponse(findPatient(userId));
    }

    // ── Appointments ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getMyAppointments(Long userId, Pageable pageable) {
        Patient patient = findPatient(userId);
        return appointmentRepository
                .findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patient.getId(), pageable)
                .map(appt -> toAppointmentResponse(appt, patient));
    }

    // ── Prescriptions ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getMyPrescriptions(Long userId, Pageable pageable) {
        Patient patient = findPatient(userId);
        return prescriptionService.getByPatient(patient.getClinicId(), patient.getId(), pageable);
    }

    // ── Invoices ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getMyInvoices(Long userId, Pageable pageable) {
        Patient patient = findPatient(userId);
        return billingService.searchInvoices(
                patient.getClinicId(), patient.getId(),
                null, null, null, null, pageable);
    }

    // ── Notification preferences ─────────────────────────────────────────────

    @Transactional
    public PatientProfileResponse updateNotificationPreferences(
            Long userId, PatientNotificationPreferencesRequest req) {
        Patient patient = findPatient(userId);
        if (req.getSmsOptOut()   != null) patient.setSmsOptOut(req.getSmsOptOut());
        if (req.getEmailOptOut() != null) patient.setEmailOptOut(req.getEmailOptOut());
        return toProfileResponse(patientRepository.save(patient));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Patient findPatient(Long userId) {
        return patientRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN,
                        "No patient record linked to this account"));
    }

    private AppointmentResponse toAppointmentResponse(Appointment appt, Patient patient) {
        String doctorName = doctorRepository.findByIdAndDeletedFalse(appt.getDoctorId())
                .map(Doctor::getFullName).orElse(null);
        String patientName = patient.getLastName() != null
                ? patient.getFirstName() + " " + patient.getLastName()
                : patient.getFirstName();

        return AppointmentResponse.builder()
                .id(appt.getId())
                .clinicId(appt.getClinicId())
                .doctorId(appt.getDoctorId())
                .patientId(appt.getPatientId())
                .treatmentTypeId(appt.getTreatmentTypeId())
                .doctorName(doctorName)
                .patientName(patientName)
                .appointmentDate(appt.getAppointmentDate())
                .startTime(appt.getStartTime())
                .endTime(appt.getEndTime())
                .durationMins(appt.getDurationMins())
                .status(appt.getStatus())
                .reason(appt.getReason())
                .build();
    }

    private PatientProfileResponse toProfileResponse(Patient p) {
        return PatientProfileResponse.builder()
                .id(p.getId())
                .clinicId(p.getClinicId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .phone(p.getPhone())
                .email(p.getEmail())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .bloodGroup(p.getBloodGroup())
                .allergies(p.getAllergies())
                .chronicConditions(p.getChronicConditions())
                .currentMedications(p.getCurrentMedications())
                .emergencyContactName(p.getEmergencyContactName())
                .emergencyContactPhone(p.getEmergencyContactPhone())
                .emergencyContactRelation(p.getEmergencyContactRelation())
                .address(p.getAddress())
                .smsOptOut(p.isSmsOptOut())
                .emailOptOut(p.isEmailOptOut())
                .build();
    }
}
