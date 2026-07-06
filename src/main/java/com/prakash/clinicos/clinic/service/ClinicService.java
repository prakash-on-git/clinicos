package com.prakash.clinicos.clinic.service;

import com.prakash.clinicos.audit.entity.AuditAction;
import com.prakash.clinicos.audit.service.AuditService;
import com.prakash.clinicos.auth.entity.User;
import com.prakash.clinicos.auth.repository.UserRepository;
import com.prakash.clinicos.clinic.dto.request.*;
import com.prakash.clinicos.clinic.dto.response.*;
import com.prakash.clinicos.clinic.entity.*;
import com.prakash.clinicos.clinic.repository.*;
import com.prakash.clinicos.common.util.SlugUtils;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.security.UserPrincipal;
import com.prakash.clinicos.subscription.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClinicService {

    private final ClinicRepository clinicRepository;
    private final ClinicBusinessHoursRepository hoursRepository;
    private final ClinicClosureDateRepository closureRepository;
    private final ClinicSettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final AuditService auditService;

    public ClinicService(ClinicRepository clinicRepository,
                         ClinicBusinessHoursRepository hoursRepository,
                         ClinicClosureDateRepository closureRepository,
                         ClinicSettingsRepository settingsRepository,
                         UserRepository userRepository,
                         SubscriptionService subscriptionService,
                         AuditService auditService) {
        this.clinicRepository = clinicRepository;
        this.hoursRepository = hoursRepository;
        this.closureRepository = closureRepository;
        this.settingsRepository = settingsRepository;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.auditService = auditService;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public ClinicResponse createClinic(CreateClinicRequest req, UserPrincipal principal) {
        // A CLINIC_ADMIN can only own one clinic
        if (clinicRepository.existsByOwnerUserIdAndDeletedFalse(principal.getId())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "You already own a clinic. A CLINIC_ADMIN can only manage one clinic.");
        }

        String timezone = req.getTimezone() != null ? req.getTimezone() : "Asia/Kolkata";
        validateTimezone(timezone);

        String slug = generateUniqueSlug(req.getName());

        Clinic clinic = Clinic.builder()
                .name(req.getName())
                .slug(slug)
                .description(req.getDescription())
                .phone(req.getPhone())
                .email(req.getEmail())
                .website(req.getWebsite())
                .addressLine1(req.getAddressLine1())
                .addressLine2(req.getAddressLine2())
                .city(req.getCity())
                .state(req.getState())
                .postalCode(req.getPostalCode())
                .country(req.getCountry() != null ? req.getCountry() : "India")
                .timezone(timezone)
                .ownerUserId(principal.getId())
                .build();

        final Clinic savedClinic = clinicRepository.save(clinic);

        // Auto-create settings with defaults in the same transaction
        ClinicSettings settings = ClinicSettings.builder().clinic(savedClinic).build();
        settingsRepository.save(settings);

        // Link this clinic to the owning user (enables GET /clinics/mine)
        userRepository.findById(principal.getId()).ifPresent(user -> {
            user.setClinicId(savedClinic.getId());
            userRepository.save(user);
        });

        // Auto-assign FREE subscription plan to the new clinic
        subscriptionService.autoAssignFree(savedClinic.getId());

        log.info("Clinic created: id={}, slug={}, owner={}", savedClinic.getId(), slug, principal.getEmail());
        ClinicResponse created = toFullResponse(savedClinic);
        auditService.log(savedClinic.getId(), "CLINIC", savedClinic.getId(), AuditAction.CREATE, null, created, principal.getId());
        return created;
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ClinicResponse getClinicById(Long clinicId) {
        Clinic clinic = findClinicOrThrow(clinicId);
        return toFullResponse(clinic);
    }

    @Transactional(readOnly = true)
    public ClinicResponse getMyClinic(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getClinicId() == null) {
            throw new AppException(HttpStatus.NOT_FOUND,
                    "You have not created a clinic yet. POST /api/v1/clinics to create one.");
        }

        Clinic clinic = findClinicOrThrow(user.getClinicId());
        assertOwnership(clinic, principal);
        return toFullResponse(clinic);
    }

    @Transactional(readOnly = true)
    public Page<ClinicResponse> listClinics(String city, Pageable pageable) {
        Page<Clinic> clinics = (city != null && !city.isBlank())
                ? clinicRepository.findAllByDeletedFalseAndCityIgnoreCase(city, pageable)
                : clinicRepository.findAllByDeletedFalse(pageable);

        // Return lean responses for the list (no nested hours/closures/settings)
        return clinics.map(this::toSummaryResponse);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public ClinicResponse updateClinic(Long clinicId, UpdateClinicRequest req, UserPrincipal principal) {
        Clinic clinic = findClinicOrThrow(clinicId);
        assertOwnership(clinic, principal);

        ClinicResponse before = toFullResponse(clinic);

        if (req.getName() != null)         clinic.setName(req.getName());
        if (req.getDescription() != null)  clinic.setDescription(req.getDescription());
        if (req.getPhone() != null)        clinic.setPhone(req.getPhone());
        if (req.getEmail() != null)        clinic.setEmail(req.getEmail());
        if (req.getWebsite() != null)      clinic.setWebsite(req.getWebsite());
        if (req.getAddressLine1() != null) clinic.setAddressLine1(req.getAddressLine1());
        if (req.getAddressLine2() != null) clinic.setAddressLine2(req.getAddressLine2());
        if (req.getCity() != null)         clinic.setCity(req.getCity());
        if (req.getState() != null)        clinic.setState(req.getState());
        if (req.getPostalCode() != null)   clinic.setPostalCode(req.getPostalCode());
        if (req.getCountry() != null)      clinic.setCountry(req.getCountry());
        if (req.getAlwaysOpen() != null)   clinic.setAlwaysOpen(req.getAlwaysOpen());

        if (req.getTimezone() != null) {
            validateTimezone(req.getTimezone());
            clinic.setTimezone(req.getTimezone());
        }

        clinic = clinicRepository.save(clinic);
        ClinicResponse after = toFullResponse(clinic);
        auditService.log(clinicId, "CLINIC", clinicId, AuditAction.UPDATE, before, after, principal.getId());
        return after;
    }

    // ── Soft Delete ───────────────────────────────────────────────────────────

    @Transactional
    public void deleteClinic(Long clinicId, UserPrincipal principal) {
        Clinic clinic = findClinicOrThrow(clinicId);
        assertOwnership(clinic, principal);

        if (clinic.isDeleted()) {
            throw new AppException(HttpStatus.CONFLICT, "Clinic is already deleted");
        }

        clinic.setDeleted(true);
        clinic.setDeletedAt(LocalDateTime.now());
        clinic.setDeletedBy(principal.getId());
        clinicRepository.save(clinic);

        // Unlink the user so they can create a new clinic if needed
        userRepository.findById(principal.getId()).ifPresent(user -> {
            user.setClinicId(null);
            userRepository.save(user);
        });

        log.info("Clinic soft-deleted: id={}, by={}", clinicId, principal.getEmail());
    }

    // ── Emergency Close / Reopen ──────────────────────────────────────────────

    @Transactional
    public ClinicResponse emergencyClose(Long clinicId, EmergencyCloseRequest req, UserPrincipal principal) {
        Clinic clinic = findClinicOrThrow(clinicId);
        assertOwnership(clinic, principal);

        if (clinic.isEmergencyClosed()) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Clinic is already emergency-closed. Use /emergency-reopen first.");
        }

        clinic.setEmergencyClosed(true);
        clinic.setEmergencyCloseReason(req.getReason());
        clinic.setEmergencyClosedAt(LocalDateTime.now());
        clinic.setEmergencyClosedBy(principal.getId());
        clinic = clinicRepository.save(clinic);

        log.info("Clinic emergency-closed: id={}, by={}, reason={}", clinicId, principal.getEmail(), req.getReason());
        return toFullResponse(clinic);
    }

    @Transactional
    public ClinicResponse emergencyReopen(Long clinicId, UserPrincipal principal) {
        Clinic clinic = findClinicOrThrow(clinicId);
        assertOwnership(clinic, principal);

        if (!clinic.isEmergencyClosed()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Clinic is not currently emergency-closed");
        }

        clinic.setEmergencyClosed(false);
        clinic.setEmergencyCloseReason(null);
        clinic.setEmergencyClosedAt(null);
        clinic.setEmergencyClosedBy(null);
        clinic = clinicRepository.save(clinic);

        log.info("Clinic emergency-reopened: id={}, by={}", clinicId, principal.getEmail());
        return toFullResponse(clinic);
    }

    // ── Business Hours ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BusinessHoursResponse> getBusinessHours(Long clinicId) {
        findClinicOrThrow(clinicId); // verify clinic exists
        return hoursRepository.findByClinicIdOrderByDayOfWeekAscOpenTimeAsc(clinicId)
                .stream().map(this::toHoursResponse).toList();
    }

    /**
     * Replaces ALL business hours for the clinic with the submitted list.
     *
     * Strategy: delete all existing → insert new (simpler than diffing).
     * This is safe within @Transactional — if validation fails, nothing is deleted.
     *
     * Validates per day:
     *   - close > open for each shift
     *   - no overlapping shifts on the same day
     *   - max 6 shifts per day (practical limit)
     */
    @Transactional
    public List<BusinessHoursResponse> updateAllHours(Long clinicId,
                                                      List<DayShiftRequest> shifts,
                                                      UserPrincipal principal) {
        Clinic clinic = findClinicOrThrow(clinicId);
        assertOwnership(clinic, principal);

        validateShifts(shifts);

        // Delete existing, then insert new (all in one transaction)
        hoursRepository.deleteByClinicId(clinicId);
        hoursRepository.flush(); // ensure deletes are flushed before inserts

        List<ClinicBusinessHours> newHours = shifts.stream()
                .map(s -> ClinicBusinessHours.builder()
                        .clinic(clinic)
                        .dayOfWeek(s.getDayOfWeek())
                        .openTime(s.getOpenTime())
                        .closeTime(s.getCloseTime())
                        .shiftLabel(s.getShiftLabel())
                        .build())
                .collect(Collectors.toList());

        hoursRepository.saveAll(newHours);
        return hoursRepository.findByClinicIdOrderByDayOfWeekAscOpenTimeAsc(clinicId)
                .stream().map(this::toHoursResponse).toList();
    }

    /**
     * Replaces ALL shifts for a specific day of the week.
     * Leaves other days untouched.
     */
    @Transactional
    public List<BusinessHoursResponse> updateDayHours(Long clinicId,
                                                       DayOfWeek day,
                                                       List<ShiftRequest> shifts,
                                                       UserPrincipal principal) {
        Clinic clinic = findClinicOrThrow(clinicId);
        assertOwnership(clinic, principal);

        validateDayShifts(day, shifts);

        hoursRepository.deleteByClinicIdAndDayOfWeek(clinicId, day);
        hoursRepository.flush();

        List<ClinicBusinessHours> newHours = shifts.stream()
                .map(s -> ClinicBusinessHours.builder()
                        .clinic(clinic)
                        .dayOfWeek(day)
                        .openTime(s.getOpenTime())
                        .closeTime(s.getCloseTime())
                        .shiftLabel(s.getShiftLabel())
                        .build())
                .collect(Collectors.toList());

        hoursRepository.saveAll(newHours);
        return hoursRepository.findByClinicIdAndDayOfWeek(clinicId, day)
                .stream().map(this::toHoursResponse).toList();
    }

    /**
     * Removes ALL shifts for a day → clinic is closed that day.
     */
    @Transactional
    public void removeDayHours(Long clinicId, DayOfWeek day, UserPrincipal principal) {
        Clinic clinic = findClinicOrThrow(clinicId);
        assertOwnership(clinic, principal);
        hoursRepository.deleteByClinicIdAndDayOfWeek(clinicId, day);
    }

    // ── Closure Dates ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ClosureDateResponse> getClosureDates(Long clinicId, boolean upcomingOnly) {
        findClinicOrThrow(clinicId);
        List<ClinicClosureDate> dates = upcomingOnly
                ? closureRepository
                .findByClinicIdAndClosureDateGreaterThanEqualOrderByClosureDateAsc(clinicId, LocalDate.now())
                : closureRepository.findByClinicIdOrderByClosureDateAsc(clinicId);
        return dates.stream().map(this::toClosureResponse).toList();
    }

    @Transactional
    public ClosureDateResponse addClosureDate(Long clinicId, ClosureDateRequest req, UserPrincipal principal) {
        Clinic clinic = findClinicOrThrow(clinicId);
        assertOwnership(clinic, principal);

        if (closureRepository.existsByClinicIdAndClosureDate(clinicId, req.getClosureDate())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "A closure already exists for " + req.getClosureDate());
        }

        ClinicClosureDate closure = ClinicClosureDate.builder()
                .clinic(clinic)
                .closureDate(req.getClosureDate())
                .reason(req.getReason())
                .build();

        closure = closureRepository.save(closure);
        return toClosureResponse(closure);
    }

    @Transactional
    public void removeClosureDate(Long clinicId, Long closureId, UserPrincipal principal) {
        findClinicOrThrow(clinicId);
        assertOwnership(findClinicOrThrow(clinicId), principal);

        ClinicClosureDate closure = closureRepository.findByIdAndClinicId(closureId, clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Closure date not found"));

        closureRepository.delete(closure);
    }

    // ── Settings ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ClinicSettingsResponse getSettings(Long clinicId) {
        findClinicOrThrow(clinicId);
        ClinicSettings settings = settingsRepository.findByClinicId(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Settings not found"));
        return toSettingsResponse(settings);
    }

    @Transactional
    public ClinicSettingsResponse updateSettings(Long clinicId, ClinicSettingsRequest req, UserPrincipal principal) {
        Clinic clinic = findClinicOrThrow(clinicId);
        assertOwnership(clinic, principal);

        ClinicSettings settings = settingsRepository.findByClinicId(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Settings not found"));

        settings.setAppointmentDurationMins(req.getAppointmentDurationMins());
        settings.setAdvanceBookingDays(req.getAdvanceBookingDays());
        settings.setCancellationWindowHours(req.getCancellationWindowHours());
        settings.setMaxPatientsPerDay(req.getMaxPatientsPerDay());
        settings.setAllowWalkIns(req.getAllowWalkIns());
        settings.setAutoConfirmAppointments(req.getAutoConfirmAppointments());

        return toSettingsResponse(settingsRepository.save(settings));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Clinic findClinicOrThrow(Long clinicId) {
        return clinicRepository.findByIdAndDeletedFalse(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Clinic not found with id: " + clinicId));
    }

    /**
     * Ownership check: SUPER_ADMIN can manage any clinic; CLINIC_ADMIN only their own.
     * Called before every mutating service method.
     */
    private void assertOwnership(Clinic clinic, UserPrincipal principal) {
        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!isSuperAdmin && !clinic.getOwnerUserId().equals(principal.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "You do not have permission to manage this clinic");
        }
    }

    /**
     * Computes whether the clinic is currently open based on:
     *   1. Deleted? → always closed
     *   2. Emergency closed? → always closed
     *   3. 24/7? → always open
     *   4. Today is a closure date? → closed
     *   5. Current local time falls within any shift window for today? → open/closed
     *
     * Uses clinic's IANA timezone for correct local time computation.
     */
    private boolean computeIsCurrentlyOpen(Clinic clinic) {
        if (clinic.isDeleted()) return false;
        if (clinic.isEmergencyClosed()) return false;
        if (clinic.isAlwaysOpen()) return true;

        ZoneId zone;
        try {
            zone = ZoneId.of(clinic.getTimezone());
        } catch (DateTimeException e) {
            return false; // invalid timezone — treat as closed, don't crash
        }

        ZonedDateTime now = ZonedDateTime.now(zone);
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        DayOfWeek currentDay = now.getDayOfWeek();

        // Is today a planned closure date?
        if (closureRepository.existsByClinicIdAndClosureDate(clinic.getId(), today)) {
            return false;
        }

        // Does current local time fall within any shift window for today?
        List<ClinicBusinessHours> todayShifts =
                hoursRepository.findByClinicIdAndDayOfWeek(clinic.getId(), currentDay);

        return todayShifts.stream().anyMatch(shift ->
                !currentTime.isBefore(shift.getOpenTime()) &&
                        currentTime.isBefore(shift.getCloseTime())
        );
    }

    /**
     * Validates all shifts in a bulk request.
     * Groups by day and checks for overlaps within each day.
     */
    private void validateShifts(List<DayShiftRequest> shifts) {
        // Group shifts by day
        Map<DayOfWeek, List<DayShiftRequest>> byDay = shifts.stream()
                .collect(Collectors.groupingBy(DayShiftRequest::getDayOfWeek));

        for (Map.Entry<DayOfWeek, List<DayShiftRequest>> entry : byDay.entrySet()) {
            List<DayShiftRequest> dayShifts = entry.getValue();

            if (dayShifts.size() > 6) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Maximum 6 shifts per day allowed on " + entry.getKey());
            }

            // Validate each shift: close > open
            for (DayShiftRequest shift : dayShifts) {
                if (!shift.getCloseTime().isAfter(shift.getOpenTime())) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            String.format("closeTime must be after openTime on %s: %s–%s",
                                    shift.getDayOfWeek(), shift.getOpenTime(), shift.getCloseTime()));
                }
            }

            // Check overlaps: sort by openTime then check consecutive pairs
            List<DayShiftRequest> sorted = dayShifts.stream()
                    .sorted(Comparator.comparing(DayShiftRequest::getOpenTime))
                    .toList();

            for (int i = 0; i < sorted.size() - 1; i++) {
                DayShiftRequest a = sorted.get(i);
                DayShiftRequest b = sorted.get(i + 1);
                if (a.getCloseTime().isAfter(b.getOpenTime())) {
                    throw new AppException(HttpStatus.CONFLICT,
                            String.format("Overlapping shifts on %s: %s–%s conflicts with %s–%s",
                                    entry.getKey(),
                                    a.getOpenTime(), a.getCloseTime(),
                                    b.getOpenTime(), b.getCloseTime()));
                }
            }
        }
    }

    private void validateDayShifts(DayOfWeek day, List<ShiftRequest> shifts) {
        if (shifts.size() > 6) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Maximum 6 shifts per day allowed on " + day);
        }
        for (ShiftRequest shift : shifts) {
            if (!shift.getCloseTime().isAfter(shift.getOpenTime())) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        String.format("closeTime must be after openTime: %s–%s",
                                shift.getOpenTime(), shift.getCloseTime()));
            }
        }
        List<ShiftRequest> sorted = shifts.stream()
                .sorted(Comparator.comparing(ShiftRequest::getOpenTime))
                .toList();
        for (int i = 0; i < sorted.size() - 1; i++) {
            ShiftRequest a = sorted.get(i);
            ShiftRequest b = sorted.get(i + 1);
            if (a.getCloseTime().isAfter(b.getOpenTime())) {
                throw new AppException(HttpStatus.CONFLICT,
                        String.format("Overlapping shifts on %s: %s–%s conflicts with %s–%s",
                                day, a.getOpenTime(), a.getCloseTime(),
                                b.getOpenTime(), b.getCloseTime()));
            }
        }
    }

    private void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException e) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Invalid timezone: '" + timezone + "'. Use IANA format e.g. 'Asia/Kolkata'.");
        }
    }

    private String generateUniqueSlug(String name) {
        String base = SlugUtils.slugify(name);
        String slug = base;
        int counter = 1;
        while (clinicRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }

    // ── Response mappers ──────────────────────────────────────────────────────

    /** Full response: includes hours, upcoming closures, settings. */
    private ClinicResponse toFullResponse(Clinic clinic) {
        List<ClinicBusinessHours> hours =
                hoursRepository.findByClinicIdOrderByDayOfWeekAscOpenTimeAsc(clinic.getId());
        List<ClosureDateResponse> upcomingClosures = closureRepository
                .findByClinicIdAndClosureDateGreaterThanEqualOrderByClosureDateAsc(
                        clinic.getId(), LocalDate.now())
                .stream().map(this::toClosureResponse).toList();
        ClinicSettings settings = settingsRepository.findByClinicId(clinic.getId()).orElse(null);

        return toResponseBuilder(clinic)
                .businessHours(hours.stream().map(this::toHoursResponse).toList())
                .upcomingClosures(upcomingClosures)
                .settings(settings != null ? toSettingsResponse(settings) : null)
                .build();
    }

    /** Lean summary for paginated list — no nested collections. */
    private ClinicResponse toSummaryResponse(Clinic clinic) {
        return toResponseBuilder(clinic).build();
    }

    /** Shared builder — fields common to both full and summary responses. */
    private ClinicResponse.ClinicResponseBuilder toResponseBuilder(Clinic clinic) {
        return ClinicResponse.builder()
                .id(clinic.getId())
                .name(clinic.getName())
                .slug(clinic.getSlug())
                .description(clinic.getDescription())
                .logoUrl(clinic.getLogoUrl())
                .phone(clinic.getPhone())
                .email(clinic.getEmail())
                .website(clinic.getWebsite())
                .addressLine1(clinic.getAddressLine1())
                .addressLine2(clinic.getAddressLine2())
                .city(clinic.getCity())
                .state(clinic.getState())
                .postalCode(clinic.getPostalCode())
                .country(clinic.getCountry())
                .timezone(clinic.getTimezone())
                .ownerUserId(clinic.getOwnerUserId())
                .alwaysOpen(clinic.isAlwaysOpen())
                .emergencyClosed(clinic.isEmergencyClosed())
                .emergencyCloseReason(clinic.getEmergencyCloseReason())
                .emergencyClosedAt(clinic.getEmergencyClosedAt())
                .deleted(clinic.isDeleted())
                .currentlyOpen(computeIsCurrentlyOpen(clinic))
                .createdAt(clinic.getCreatedAt())
                .updatedAt(clinic.getUpdatedAt());
    }

    private BusinessHoursResponse toHoursResponse(ClinicBusinessHours h) {
        return BusinessHoursResponse.builder()
                .id(h.getId())
                .dayOfWeek(h.getDayOfWeek())
                .openTime(h.getOpenTime())
                .closeTime(h.getCloseTime())
                .shiftLabel(h.getShiftLabel())
                .build();
    }

    private ClosureDateResponse toClosureResponse(ClinicClosureDate c) {
        return ClosureDateResponse.builder()
                .id(c.getId())
                .closureDate(c.getClosureDate())
                .reason(c.getReason())
                .build();
    }

    private ClinicSettingsResponse toSettingsResponse(ClinicSettings s) {
        return ClinicSettingsResponse.builder()
                .appointmentDurationMins(s.getAppointmentDurationMins())
                .advanceBookingDays(s.getAdvanceBookingDays())
                .cancellationWindowHours(s.getCancellationWindowHours())
                .maxPatientsPerDay(s.getMaxPatientsPerDay())
                .allowWalkIns(s.isAllowWalkIns())
                .autoConfirmAppointments(s.isAutoConfirmAppointments())
                .build();
    }
}
