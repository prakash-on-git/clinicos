package com.prakash.clinicos.doctor.service;

import com.prakash.clinicos.audit.entity.AuditAction;
import com.prakash.clinicos.audit.service.AuditService;
import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.common.util.SlugUtils;
import com.prakash.clinicos.doctor.dto.request.*;
import com.prakash.clinicos.doctor.dto.response.*;
import com.prakash.clinicos.doctor.entity.*;
import com.prakash.clinicos.doctor.repository.*;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.security.UserPrincipal;
import com.prakash.clinicos.subscription.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.prakash.clinicos.config.RedisConfig.DOCTOR_AVAILABILITY_CACHE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorDayOverrideRepository overrideRepository;
    private final DoctorLeaveDateRepository leaveRepository;
    private final TreatmentTypeRepository treatmentTypeRepository;
    private final DoctorTreatmentRepository doctorTreatmentRepository;
    private final ClinicRepository clinicRepository;
    private final SubscriptionService subscriptionService;
    private final AuditService auditService;

    public DoctorService(DoctorRepository doctorRepository,
                         DoctorDayOverrideRepository overrideRepository,
                         DoctorLeaveDateRepository leaveRepository,
                         TreatmentTypeRepository treatmentTypeRepository,
                         DoctorTreatmentRepository doctorTreatmentRepository,
                         ClinicRepository clinicRepository,
                         SubscriptionService subscriptionService,
                         AuditService auditService) {
        this.doctorRepository = doctorRepository;
        this.overrideRepository = overrideRepository;
        this.leaveRepository = leaveRepository;
        this.treatmentTypeRepository = treatmentTypeRepository;
        this.doctorTreatmentRepository = doctorTreatmentRepository;
        this.clinicRepository = clinicRepository;
        this.subscriptionService = subscriptionService;
        this.auditService = auditService;
    }

    // ── Doctor CRUD ───────────────────────────────────────────────────────────

    @Transactional
    public DoctorResponse createDoctor(Long clinicId, CreateDoctorRequest req, UserPrincipal principal) {
        // Validate clinic exists and caller owns it
        clinicRepository.findByIdAndDeletedFalse(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Clinic not found with id: " + clinicId));
        assertClinicOwnership(clinicId, principal);

        // Enforce subscription plan doctor limit
        subscriptionService.enforceDoctorLimit(clinicId);

        // Email uniqueness within clinic
        if (req.getEmail() != null
                && doctorRepository.existsByClinicIdAndEmailAndDeletedFalse(clinicId, req.getEmail())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "A doctor with email '" + req.getEmail() + "' already exists in this clinic");
        }

        // Validate userId if provided
        if (req.getUserId() != null
                && doctorRepository.existsByUserIdAndDeletedFalse(req.getUserId())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "User " + req.getUserId() + " is already linked to another doctor profile");
        }

        String slug = generateUniqueSlug(req.getFullName());

        Doctor doctor = Doctor.builder()
                .clinicId(clinicId)
                .userId(req.getUserId())
                .fullName(req.getFullName())
                .slug(slug)
                .email(req.getEmail())
                .phone(req.getPhone())
                .qualification(req.getQualification())
                .specialization(req.getSpecialization())
                .bio(req.getBio())
                .avatarUrl(req.getAvatarUrl())
                .registrationNumber(req.getRegistrationNumber())
                .consultationFee(req.getConsultationFee())
                .build();

        doctor = doctorRepository.save(doctor);
        log.info("Doctor created: id={}, slug={}, clinic={}", doctor.getId(), slug, clinicId);
        DoctorResponse created = toFullResponse(doctor);
        auditService.log(clinicId, "DOCTOR", doctor.getId(), AuditAction.CREATE, null, created, principal.getId());
        return created;
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long doctorId) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        return toFullResponse(doctor);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorBySlug(String slug) {
        Doctor doctor = doctorRepository.findBySlugAndDeletedFalse(slug)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Doctor not found with slug: " + slug));
        return toFullResponse(doctor);
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponse> listDoctorsByClinic(Long clinicId, boolean activeOnly, Pageable pageable) {
        clinicRepository.findByIdAndDeletedFalse(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Clinic not found with id: " + clinicId));

        Page<Doctor> doctors = activeOnly
                ? doctorRepository.findByClinicIdAndDeletedFalseAndActive(clinicId, true, pageable)
                : doctorRepository.findByClinicIdAndDeletedFalse(clinicId, pageable);

        return doctors.map(this::toSummaryResponse);
    }

    @Transactional
    @CacheEvict(value = DOCTOR_AVAILABILITY_CACHE, allEntries = true)
    public DoctorResponse updateDoctor(Long clinicId, Long doctorId,
                                        UpdateDoctorRequest req, UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertSameClinic(doctor, clinicId);
        assertOwnership(doctor, principal);

        // Email uniqueness check if email is being changed
        if (req.getEmail() != null && !req.getEmail().equals(doctor.getEmail())
                && doctorRepository.existsByClinicIdAndEmailAndDeletedFalse(clinicId, req.getEmail())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "A doctor with email '" + req.getEmail() + "' already exists in this clinic");
        }

        DoctorResponse beforeDoctor = toFullResponse(doctor);

        if (req.getFullName() != null)          doctor.setFullName(req.getFullName());
        if (req.getEmail() != null)             doctor.setEmail(req.getEmail());
        if (req.getPhone() != null)             doctor.setPhone(req.getPhone());
        if (req.getQualification() != null)     doctor.setQualification(req.getQualification());
        if (req.getSpecialization() != null)    doctor.setSpecialization(req.getSpecialization());
        if (req.getBio() != null)               doctor.setBio(req.getBio());
        if (req.getAvatarUrl() != null)         doctor.setAvatarUrl(req.getAvatarUrl());
        if (req.getRegistrationNumber() != null) doctor.setRegistrationNumber(req.getRegistrationNumber());
        if (req.getConsultationFee() != null)   doctor.setConsultationFee(req.getConsultationFee());
        if (req.getActive() != null)            doctor.setActive(req.getActive());

        doctor = doctorRepository.save(doctor);
        DoctorResponse afterDoctor = toFullResponse(doctor);
        auditService.log(clinicId, "DOCTOR", doctorId, AuditAction.UPDATE, beforeDoctor, afterDoctor, principal.getId());
        return afterDoctor;
    }

    @Transactional
    public void deleteDoctor(Long clinicId, Long doctorId, UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertSameClinic(doctor, clinicId);
        assertOwnership(doctor, principal);

        if (doctor.isDeleted()) {
            throw new AppException(HttpStatus.CONFLICT, "Doctor is already deleted");
        }

        DoctorResponse beforeDelete = toFullResponse(doctor);
        doctor.setDeleted(true);
        doctor.setDeletedAt(LocalDateTime.now());
        doctor.setDeletedBy(principal.getId());
        doctorRepository.save(doctor);
        log.info("Doctor soft-deleted: id={}, by={}", doctorId, principal.getEmail());
        auditService.log(clinicId, "DOCTOR", doctorId, AuditAction.DELETE, beforeDelete, null, principal.getId());
    }

    @Transactional
    public DoctorResponse linkUserAccount(Long clinicId, Long doctorId, Long userId, UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertSameClinic(doctor, clinicId);
        assertOwnership(doctor, principal);

        if (doctorRepository.existsByUserIdAndDeletedFalse(userId)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "User " + userId + " is already linked to another doctor profile");
        }

        doctor.setUserId(userId);
        return toFullResponse(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorResponse unlinkUserAccount(Long clinicId, Long doctorId, UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertSameClinic(doctor, clinicId);
        assertOwnership(doctor, principal);
        doctor.setUserId(null);
        return toFullResponse(doctorRepository.save(doctor));
    }

    // ── Day Overrides ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DoctorDayOverrideResponse> getUpcomingOverrides(Long clinicId, Long doctorId) {
        findDoctorOrThrow(doctorId);
        return overrideRepository
                .findByDoctorIdAndOverrideDateGreaterThanEqualOrderByOverrideDateAsc(doctorId, LocalDate.now())
                .stream().map(this::toOverrideResponse).toList();
    }

    /**
     * Upsert: if an override exists for this date, it is replaced.
     * Validates cross-field rules per override type.
     */
    @Transactional
    @CacheEvict(value = DOCTOR_AVAILABILITY_CACHE, allEntries = true)
    public DoctorDayOverrideResponse addOrUpdateOverride(Long clinicId, Long doctorId,
                                                          DoctorDayOverrideRequest req,
                                                          UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertSameClinic(doctor, clinicId);
        assertOwnership(doctor, principal);
        validateOverrideRequest(req);

        // Delete existing override for this date (upsert pattern)
        overrideRepository.deleteByDoctorIdAndOverrideDate(doctorId, req.getOverrideDate());

        DoctorDayOverride override = DoctorDayOverride.builder()
                .doctor(doctor)
                .overrideDate(req.getOverrideDate())
                .overrideType(req.getOverrideType())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .reason(req.getReason())
                .createdBy(principal.getId())
                .build();

        override = overrideRepository.save(override);
        log.info("Doctor override set: doctorId={}, date={}, type={}", doctorId,
                req.getOverrideDate(), req.getOverrideType());
        return toOverrideResponse(override);
    }

    @Transactional
    @CacheEvict(value = DOCTOR_AVAILABILITY_CACHE, allEntries = true)
    public void removeOverride(Long clinicId, Long doctorId, LocalDate date, UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertSameClinic(doctor, clinicId);
        assertOwnership(doctor, principal);

        if (!overrideRepository.existsByDoctorIdAndOverrideDate(doctorId, date)) {
            throw new AppException(HttpStatus.NOT_FOUND,
                    "No override found for doctor " + doctorId + " on " + date);
        }
        overrideRepository.deleteByDoctorIdAndOverrideDate(doctorId, date);
    }

    // ── Leave Management ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DoctorLeaveResponse> getLeave(Long clinicId, Long doctorId, boolean upcomingOnly) {
        findDoctorOrThrow(doctorId);
        List<DoctorLeaveDate> leaves = upcomingOnly
                ? leaveRepository.findByDoctorIdAndLeaveDateGreaterThanEqualOrderByLeaveDateAsc(
                        doctorId, LocalDate.now())
                : leaveRepository.findByDoctorIdOrderByLeaveDateDesc(doctorId);
        return leaves.stream().map(this::toLeaveResponse).toList();
    }

    /**
     * Marks one or more dates as leave for a doctor.
     * Duplicate dates are rejected with 409 Conflict (skip-and-report pattern).
     * Returns all successfully created leave entries.
     */
    @Transactional
    @CacheEvict(value = DOCTOR_AVAILABILITY_CACHE, allEntries = true)
    public List<DoctorLeaveResponse> addLeave(Long clinicId, Long doctorId,
                                               DoctorLeaveRequest req, UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertSameClinic(doctor, clinicId);
        assertOwnership(doctor, principal);

        List<DoctorLeaveDate> created = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();

        for (LocalDate date : req.getLeaveDates()) {
            if (leaveRepository.existsByDoctorIdAndLeaveDate(doctorId, date)) {
                conflicts.add(date.toString());
                continue;
            }
            DoctorLeaveDate leave = DoctorLeaveDate.builder()
                    .doctor(doctor)
                    .leaveDate(date)
                    .leaveType(req.getLeaveType())
                    .reason(req.getReason())
                    .createdBy(principal.getId())
                    .build();
            created.add(leaveRepository.save(leave));
        }

        if (!conflicts.isEmpty() && created.isEmpty()) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Leave already exists for all provided dates: " + conflicts);
        }

        if (!conflicts.isEmpty()) {
            log.warn("Leave already existed for dates (skipped): {}", conflicts);
        }

        log.info("Doctor leave added: doctorId={}, dates={}", doctorId, created.size());
        return created.stream().map(this::toLeaveResponse).toList();
    }

    @Transactional
    @CacheEvict(value = DOCTOR_AVAILABILITY_CACHE, allEntries = true)
    public void removeLeave(Long clinicId, Long doctorId, LocalDate date, UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertSameClinic(doctor, clinicId);
        assertOwnership(doctor, principal);

        if (!leaveRepository.existsByDoctorIdAndLeaveDate(doctorId, date)) {
            throw new AppException(HttpStatus.NOT_FOUND,
                    "No leave found for doctor " + doctorId + " on " + date);
        }
        leaveRepository.deleteByDoctorIdAndLeaveDate(doctorId, date);
    }

    // ── Treatment Types ───────────────────────────────────────────────────────

    @Transactional
    public TreatmentTypeResponse createTreatment(Long clinicId,
                                                  CreateTreatmentTypeRequest req,
                                                  UserPrincipal principal) {
        clinicRepository.findByIdAndDeletedFalse(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Clinic not found"));
        assertClinicOwnership(clinicId, principal);

        validateDuration(req.getDefaultDurationMins(), "defaultDurationMins");

        if (treatmentTypeRepository.existsByClinicIdAndNameAndDeletedFalse(clinicId, req.getName())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "A treatment type named '" + req.getName() + "' already exists in this clinic");
        }

        TreatmentType tt = TreatmentType.builder()
                .clinicId(clinicId)
                .name(req.getName())
                .description(req.getDescription())
                .defaultDurationMins(req.getDefaultDurationMins())
                .defaultFee(req.getDefaultFee())
                .colorHex(req.getColorHex())
                .build();

        return toTreatmentResponse(treatmentTypeRepository.save(tt));
    }

    @Transactional(readOnly = true)
    public List<TreatmentTypeResponse> listTreatments(Long clinicId, boolean activeOnly) {
        List<TreatmentType> types = activeOnly
                ? treatmentTypeRepository.findByClinicIdAndDeletedFalseAndActiveOrderByNameAsc(clinicId, true)
                : treatmentTypeRepository.findByClinicIdAndDeletedFalseOrderByNameAsc(clinicId);
        return types.stream().map(this::toTreatmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public TreatmentTypeResponse getTreatment(Long clinicId, Long treatmentId) {
        TreatmentType tt = treatmentTypeRepository.findByIdAndClinicIdAndDeletedFalse(treatmentId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Treatment type not found with id: " + treatmentId));
        return toTreatmentResponse(tt);
    }

    @Transactional
    public TreatmentTypeResponse updateTreatment(Long clinicId, Long treatmentId,
                                                  UpdateTreatmentTypeRequest req,
                                                  UserPrincipal principal) {
        assertClinicOwnership(clinicId, principal);
        TreatmentType tt = treatmentTypeRepository.findByIdAndClinicIdAndDeletedFalse(treatmentId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Treatment type not found with id: " + treatmentId));

        if (req.getName() != null) {
            if (!req.getName().equals(tt.getName())
                    && treatmentTypeRepository.existsByClinicIdAndNameAndDeletedFalse(clinicId, req.getName())) {
                throw new AppException(HttpStatus.CONFLICT,
                        "A treatment named '" + req.getName() + "' already exists");
            }
            tt.setName(req.getName());
        }
        if (req.getDescription() != null)         tt.setDescription(req.getDescription());
        if (req.getDefaultDurationMins() != null) {
            validateDuration(req.getDefaultDurationMins(), "defaultDurationMins");
            tt.setDefaultDurationMins(req.getDefaultDurationMins());
        }
        if (req.getDefaultFee() != null)          tt.setDefaultFee(req.getDefaultFee());
        if (req.getColorHex() != null)            tt.setColorHex(req.getColorHex());
        if (req.getActive() != null)              tt.setActive(req.getActive());

        return toTreatmentResponse(treatmentTypeRepository.save(tt));
    }

    @Transactional
    public void deleteTreatment(Long clinicId, Long treatmentId, UserPrincipal principal) {
        assertClinicOwnership(clinicId, principal);
        TreatmentType tt = treatmentTypeRepository.findByIdAndClinicIdAndDeletedFalse(treatmentId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Treatment type not found with id: " + treatmentId));

        tt.setDeleted(true);
        tt.setDeletedAt(LocalDateTime.now());
        tt.setDeletedBy(principal.getId());
        treatmentTypeRepository.save(tt);
    }

    // ── Doctor Treatments ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DoctorTreatmentResponse> getDoctorTreatments(Long clinicId, Long doctorId) {
        findDoctorOrThrow(doctorId);
        return doctorTreatmentRepository.findByDoctorIdAndActiveTrue(doctorId)
                .stream().map(this::toDoctorTreatmentResponse).toList();
    }

    /**
     * Add or update a treatment for a doctor (upsert by treatmentTypeId).
     * If already linked, updates customDurationMins and customFee.
     */
    @Transactional
    public DoctorTreatmentResponse addOrUpdateDoctorTreatment(Long clinicId, Long doctorId,
                                                               Long treatmentTypeId,
                                                               DoctorTreatmentRequest req,
                                                               UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertSameClinic(doctor, clinicId);
        assertOwnership(doctor, principal);

        TreatmentType tt = treatmentTypeRepository.findByIdAndClinicIdAndDeletedFalse(treatmentTypeId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Treatment type not found: " + treatmentTypeId));

        if (req.getCustomDurationMins() != null) {
            validateDuration(req.getCustomDurationMins(), "customDurationMins");
        }

        DoctorTreatment dt = doctorTreatmentRepository
                .findByDoctorIdAndTreatmentTypeId(doctorId, treatmentTypeId)
                .orElse(DoctorTreatment.builder().doctor(doctor).treatmentType(tt).build());

        dt.setCustomDurationMins(req.getCustomDurationMins());
        dt.setCustomFee(req.getCustomFee());
        if (req.getActive() != null) dt.setActive(req.getActive());

        return toDoctorTreatmentResponse(doctorTreatmentRepository.save(dt));
    }

    @Transactional
    public void removeDoctorTreatment(Long clinicId, Long doctorId, Long treatmentTypeId,
                                       UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertSameClinic(doctor, clinicId);
        assertOwnership(doctor, principal);

        if (!doctorTreatmentRepository.existsByDoctorIdAndTreatmentTypeId(doctorId, treatmentTypeId)) {
            throw new AppException(HttpStatus.NOT_FOUND,
                    "Doctor does not offer treatment type: " + treatmentTypeId);
        }
        doctorTreatmentRepository.deleteByDoctorIdAndTreatmentTypeId(doctorId, treatmentTypeId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Doctor findDoctorOrThrow(Long doctorId) {
        return doctorRepository.findByIdAndDeletedFalse(doctorId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Doctor not found with id: " + doctorId));
    }

    private void assertSameClinic(Doctor doctor, Long clinicId) {
        if (!doctor.getClinicId().equals(clinicId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Doctor not found in this clinic");
        }
    }

    /**
     * CLINIC_ADMIN can only manage doctors in their own clinic.
     * SUPER_ADMIN can manage any clinic.
     */
    private void assertOwnership(Doctor doctor, UserPrincipal principal) {
        assertClinicOwnership(doctor.getClinicId(), principal);
    }

    private void assertClinicOwnership(Long clinicId, UserPrincipal principal) {
        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) return;

        clinicRepository.findByIdAndDeletedFalse(clinicId).ifPresent(clinic -> {
            if (!clinic.getOwnerUserId().equals(principal.getId())) {
                throw new AppException(HttpStatus.FORBIDDEN,
                        "You do not have permission to manage this clinic's doctors");
            }
        });
    }

    /**
     * Validates that a duration is a positive multiple of 10 minutes.
     * The DB CHECK constraint enforces this at persistence time;
     * this service-layer check gives a cleaner error message.
     */
    private void validateDuration(int duration, String fieldName) {
        if (duration <= 0 || duration % 10 != 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    fieldName + " must be a positive multiple of 10 minutes "
                            + "(e.g. 10, 20, 30, 60). Got: " + duration);
        }
    }

    private void validateOverrideRequest(DoctorDayOverrideRequest req) {
        switch (req.getOverrideType()) {
            case DAY_OFF -> {
                if (req.getStartTime() != null || req.getEndTime() != null) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "DAY_OFF override must not have startTime or endTime");
                }
            }
            case LATE_START -> {
                if (req.getStartTime() == null) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "LATE_START override requires startTime (the new effective start time)");
                }
                if (req.getEndTime() != null) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "LATE_START override must not have endTime. "
                                    + "Use CUSTOM_HOURS if you need both late start and early end.");
                }
            }
            case EARLY_END -> {
                if (req.getEndTime() == null) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "EARLY_END override requires endTime (the new effective end time)");
                }
                if (req.getStartTime() != null) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "EARLY_END override must not have startTime. "
                                    + "Use CUSTOM_HOURS if you need both late start and early end.");
                }
            }
            case CUSTOM_HOURS -> {
                if (req.getStartTime() == null || req.getEndTime() == null) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "CUSTOM_HOURS override requires both startTime and endTime");
                }
                if (!req.getEndTime().isAfter(req.getStartTime())) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "CUSTOM_HOURS: endTime must be after startTime");
                }
            }
        }
    }

    private String generateUniqueSlug(String name) {
        String base = SlugUtils.slugify(name);
        String slug = base;
        int counter = 1;
        while (doctorRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }

    // ── Response mappers ──────────────────────────────────────────────────────

    private DoctorResponse toFullResponse(Doctor doctor) {
        List<DoctorTreatmentResponse> treatments = doctorTreatmentRepository
                .findByDoctorIdAndActiveTrue(doctor.getId())
                .stream().map(this::toDoctorTreatmentResponse).toList();

        return toDoctorResponseBuilder(doctor)
                .treatments(treatments)
                .build();
    }

    private DoctorResponse toSummaryResponse(Doctor doctor) {
        return toDoctorResponseBuilder(doctor).build();
    }

    private DoctorResponse.DoctorResponseBuilder toDoctorResponseBuilder(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .clinicId(doctor.getClinicId())
                .userId(doctor.getUserId())
                .fullName(doctor.getFullName())
                .slug(doctor.getSlug())
                .email(doctor.getEmail())
                .phone(doctor.getPhone())
                .qualification(doctor.getQualification())
                .specialization(doctor.getSpecialization())
                .bio(doctor.getBio())
                .avatarUrl(doctor.getAvatarUrl())
                .registrationNumber(doctor.getRegistrationNumber())
                .consultationFee(doctor.getConsultationFee())
                .active(doctor.isActive())
                .deleted(doctor.isDeleted())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt());
    }

    private DoctorDayOverrideResponse toOverrideResponse(DoctorDayOverride o) {
        return DoctorDayOverrideResponse.builder()
                .id(o.getId())
                .overrideDate(o.getOverrideDate())
                .overrideType(o.getOverrideType().name())
                .startTime(o.getStartTime())
                .endTime(o.getEndTime())
                .reason(o.getReason())
                .build();
    }

    private DoctorLeaveResponse toLeaveResponse(DoctorLeaveDate l) {
        return DoctorLeaveResponse.builder()
                .id(l.getId())
                .leaveDate(l.getLeaveDate())
                .leaveType(l.getLeaveType().name())
                .reason(l.getReason())
                .build();
    }

    private TreatmentTypeResponse toTreatmentResponse(TreatmentType tt) {
        return TreatmentTypeResponse.builder()
                .id(tt.getId())
                .clinicId(tt.getClinicId())
                .name(tt.getName())
                .description(tt.getDescription())
                .defaultDurationMins(tt.getDefaultDurationMins())
                .defaultFee(tt.getDefaultFee())
                .colorHex(tt.getColorHex())
                .active(tt.isActive())
                .createdAt(tt.getCreatedAt())
                .updatedAt(tt.getUpdatedAt())
                .build();
    }

    private DoctorTreatmentResponse toDoctorTreatmentResponse(DoctorTreatment dt) {
        return DoctorTreatmentResponse.builder()
                .id(dt.getId())
                .treatmentTypeId(dt.getTreatmentType().getId())
                .treatmentName(dt.getTreatmentType().getName())
                .colorHex(dt.getTreatmentType().getColorHex())
                .customDurationMins(dt.getCustomDurationMins())
                .customFee(dt.getCustomFee())
                .effectiveDurationMins(dt.getEffectiveDurationMins())
                .effectiveFee(dt.getEffectiveFee())
                .active(dt.isActive())
                .build();
    }
}
