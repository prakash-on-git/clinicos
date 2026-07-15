package com.prakash.clinicos.doctor.service;

import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.doctor.dto.request.DayScheduleRequest;
import com.prakash.clinicos.doctor.dto.request.DoctorBreakRequest;
import com.prakash.clinicos.doctor.dto.response.DoctorBreakResponse;
import com.prakash.clinicos.doctor.dto.response.DoctorWeeklyScheduleResponse;
import com.prakash.clinicos.doctor.entity.*;
import com.prakash.clinicos.doctor.repository.*;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.prakash.clinicos.config.RedisConfig.DOCTOR_AVAILABILITY_CACHE;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DoctorScheduleService {

    private final DoctorRepository doctorRepository;
    private final DoctorWeeklyScheduleRepository scheduleRepository;
    private final DoctorBreakRepository breakRepository;
    private final ClinicRepository clinicRepository;

    public DoctorScheduleService(DoctorRepository doctorRepository,
                                 DoctorWeeklyScheduleRepository scheduleRepository,
                                 DoctorBreakRepository breakRepository,
                                 ClinicRepository clinicRepository) {
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
        this.breakRepository = breakRepository;
        this.clinicRepository = clinicRepository;
    }

    // ── Weekly Schedule ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DoctorWeeklyScheduleResponse> getSchedule(Long doctorId) {
        findDoctorOrThrow(doctorId);
        return scheduleRepository.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctorId)
                .stream().map(this::toScheduleResponse).toList();
    }

    /**
     * Replaces the entire weekly schedule for a doctor.
     * Same delete-all-then-insert pattern as clinic business hours.
     *
     * Validates per day:
     *   - endTime > startTime
     *   - times align to 10-minute boundaries
     *   - no overlapping shifts on the same day
     *   - max 4 shifts per day
     */
    @Transactional
    @CacheEvict(value = DOCTOR_AVAILABILITY_CACHE, allEntries = true)
    public List<DoctorWeeklyScheduleResponse> replaceFullSchedule(Long doctorId,
                                                                   List<DayScheduleRequest> shifts,
                                                                   UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertOwnership(doctor, principal);
        validateScheduleShifts(shifts);

        scheduleRepository.deleteByDoctorId(doctorId);
        scheduleRepository.flush();

        List<DoctorWeeklySchedule> newSchedule = shifts.stream()
                .map(s -> DoctorWeeklySchedule.builder()
                        .doctor(doctor)
                        .dayOfWeek(s.getDayOfWeek())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .shiftLabel(s.getShiftLabel())
                        .build())
                .collect(Collectors.toList());

        scheduleRepository.saveAll(newSchedule);
        log.info("Doctor schedule replaced: doctorId={}, shifts={}", doctorId, shifts.size());
        return scheduleRepository.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctorId)
                .stream().map(this::toScheduleResponse).toList();
    }

    /** Replace shifts for a single day. Other days are untouched. */
    @Transactional
    @CacheEvict(value = DOCTOR_AVAILABILITY_CACHE, allEntries = true)
    public List<DoctorWeeklyScheduleResponse> replaceDaySchedule(Long doctorId,
                                                                   DayOfWeek day,
                                                                   List<DayScheduleRequest> shifts,
                                                                   UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertOwnership(doctor, principal);

        // Set dayOfWeek on all entries to match the path variable (client may omit it)
        shifts.forEach(s -> s.setDayOfWeek(day));
        validateScheduleShifts(shifts);

        scheduleRepository.deleteByDoctorIdAndDayOfWeek(doctorId, day);
        scheduleRepository.flush();

        List<DoctorWeeklySchedule> newShifts = shifts.stream()
                .map(s -> DoctorWeeklySchedule.builder()
                        .doctor(doctor)
                        .dayOfWeek(day)
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .shiftLabel(s.getShiftLabel())
                        .build())
                .collect(Collectors.toList());

        scheduleRepository.saveAll(newShifts);
        return scheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, day)
                .stream().map(this::toScheduleResponse).toList();
    }

    /** Remove all shifts for a day — doctor doesn't work that weekday at all. */
    @Transactional
    @CacheEvict(value = DOCTOR_AVAILABILITY_CACHE, allEntries = true)
    public void clearDaySchedule(Long doctorId, DayOfWeek day, UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertOwnership(doctor, principal);
        scheduleRepository.deleteByDoctorIdAndDayOfWeek(doctorId, day);
        // Also clear breaks for that day since the doctor won't be working
        breakRepository.deleteByDoctorIdAndDayOfWeek(doctorId, day);
        log.info("Doctor schedule cleared for day: doctorId={}, day={}", doctorId, day);
    }

    // ── Breaks ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DoctorBreakResponse> getBreaks(Long doctorId) {
        findDoctorOrThrow(doctorId);
        return breakRepository.findByDoctorIdOrderByDayOfWeekAscBreakStartAsc(doctorId)
                .stream().map(this::toBreakResponse).toList();
    }

    /**
     * Replaces all breaks for a specific day.
     *
     * Validates:
     *   - breakEnd > breakStart
     *   - times align to 10-minute boundaries (to avoid fractional slots)
     *   - no break overlaps
     *   - breaks fall within at least one of the doctor's scheduled working windows that day
     */
    @Transactional
    @CacheEvict(value = DOCTOR_AVAILABILITY_CACHE, allEntries = true)
    public List<DoctorBreakResponse> replaceDayBreaks(Long doctorId,
                                                       DayOfWeek day,
                                                       List<DoctorBreakRequest> breaks,
                                                       UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertOwnership(doctor, principal);
        validateBreaks(doctorId, day, breaks);

        breakRepository.deleteByDoctorIdAndDayOfWeek(doctorId, day);
        breakRepository.flush();

        List<DoctorBreak> newBreaks = breaks.stream()
                .map(b -> DoctorBreak.builder()
                        .doctor(doctor)
                        .dayOfWeek(day)
                        .breakStart(b.getBreakStart())
                        .breakEnd(b.getBreakEnd())
                        .breakType(b.getBreakType() != null ? b.getBreakType() : BreakType.BREAK)
                        .label(b.getLabel())
                        .build())
                .collect(Collectors.toList());

        breakRepository.saveAll(newBreaks);
        return breakRepository.findByDoctorIdAndDayOfWeekOrderByBreakStartAsc(doctorId, day)
                .stream().map(this::toBreakResponse).toList();
    }

    @Transactional
    @CacheEvict(value = DOCTOR_AVAILABILITY_CACHE, allEntries = true)
    public void clearDayBreaks(Long doctorId, DayOfWeek day, UserPrincipal principal) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        assertOwnership(doctor, principal);
        breakRepository.deleteByDoctorIdAndDayOfWeek(doctorId, day);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Doctor findDoctorOrThrow(Long doctorId) {
        return doctorRepository.findByIdAndDeletedFalse(doctorId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Doctor not found with id: " + doctorId));
    }

    private void assertOwnership(Doctor doctor, UserPrincipal principal) {
        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) return;

        clinicRepository.findByIdAndDeletedFalse(doctor.getClinicId())
                .ifPresent(clinic -> {
                    if (!clinic.getOwnerUserId().equals(principal.getId())) {
                        throw new AppException(HttpStatus.FORBIDDEN,
                                "You do not have permission to manage this doctor");
                    }
                });
    }

    private void validateScheduleShifts(List<DayScheduleRequest> shifts) {
        Map<DayOfWeek, List<DayScheduleRequest>> byDay = shifts.stream()
                .collect(Collectors.groupingBy(DayScheduleRequest::getDayOfWeek));

        for (Map.Entry<DayOfWeek, List<DayScheduleRequest>> entry : byDay.entrySet()) {
            List<DayScheduleRequest> dayShifts = entry.getValue();
            DayOfWeek day = entry.getKey();

            if (dayShifts.size() > 4) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Maximum 4 shifts per day allowed on " + day);
            }

            for (DayScheduleRequest shift : dayShifts) {
                if (!shift.getEndTime().isAfter(shift.getStartTime())) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "endTime must be after startTime on " + day + ": "
                                    + shift.getStartTime() + "–" + shift.getEndTime());
                }
                validateTenMinuteAlignment(shift.getStartTime().getMinute(), "startTime on " + day);
                validateTenMinuteAlignment(shift.getEndTime().getMinute(), "endTime on " + day);
            }

            List<DayScheduleRequest> sorted = dayShifts.stream()
                    .sorted(Comparator.comparing(DayScheduleRequest::getStartTime))
                    .toList();

            for (int i = 0; i < sorted.size() - 1; i++) {
                DayScheduleRequest a = sorted.get(i);
                DayScheduleRequest b = sorted.get(i + 1);
                if (a.getEndTime().isAfter(b.getStartTime())) {
                    throw new AppException(HttpStatus.CONFLICT,
                            "Overlapping shifts on " + day + ": "
                                    + a.getStartTime() + "–" + a.getEndTime()
                                    + " conflicts with " + b.getStartTime() + "–" + b.getEndTime());
                }
            }
        }
    }

    private void validateBreaks(Long doctorId, DayOfWeek day, List<DoctorBreakRequest> breaks) {
        List<DoctorWeeklySchedule> workingShifts =
                scheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, day);

        for (DoctorBreakRequest b : breaks) {
            if (!b.getBreakEnd().isAfter(b.getBreakStart())) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "breakEnd must be after breakStart: "
                                + b.getBreakStart() + "–" + b.getBreakEnd());
            }
            validateTenMinuteAlignment(b.getBreakStart().getMinute(), "breakStart");
            validateTenMinuteAlignment(b.getBreakEnd().getMinute(), "breakEnd");

            // Break must fall within at least one working shift window
            boolean withinSchedule = workingShifts.stream().anyMatch(shift ->
                    !b.getBreakStart().isBefore(shift.getStartTime())
                            && !b.getBreakEnd().isAfter(shift.getEndTime()));

            if (!workingShifts.isEmpty() && !withinSchedule) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Break " + b.getBreakStart() + "–" + b.getBreakEnd()
                                + " falls outside the doctor's working hours on " + day
                                + ". Set the schedule first.");
            }
        }

        // Check break overlaps
        List<DoctorBreakRequest> sorted = breaks.stream()
                .sorted(Comparator.comparing(DoctorBreakRequest::getBreakStart))
                .toList();
        for (int i = 0; i < sorted.size() - 1; i++) {
            DoctorBreakRequest a = sorted.get(i);
            DoctorBreakRequest b = sorted.get(i + 1);
            if (a.getBreakEnd().isAfter(b.getBreakStart())) {
                throw new AppException(HttpStatus.CONFLICT,
                        "Overlapping breaks: " + a.getBreakStart() + "–" + a.getBreakEnd()
                                + " conflicts with " + b.getBreakStart() + "–" + b.getBreakEnd());
            }
        }
    }

    /**
     * Validates that a minute value aligns to a 10-minute boundary (0, 10, 20, 30, 40, 50).
     * This ensures all times map cleanly onto 10-minute slot boundaries.
     */
    private void validateTenMinuteAlignment(int minute, String fieldName) {
        if (minute % 10 != 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    fieldName + " must align to a 10-minute boundary (e.g. :00, :10, :20, :30, :40, :50). "
                            + "Got minute=" + minute);
        }
    }

    // ── Response mappers ──────────────────────────────────────────────────────

    private DoctorWeeklyScheduleResponse toScheduleResponse(DoctorWeeklySchedule s) {
        return DoctorWeeklyScheduleResponse.builder()
                .id(s.getId())
                .dayOfWeek(s.getDayOfWeek())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .shiftLabel(s.getShiftLabel())
                .build();
    }

    private DoctorBreakResponse toBreakResponse(DoctorBreak b) {
        return DoctorBreakResponse.builder()
                .id(b.getId())
                .dayOfWeek(b.getDayOfWeek())
                .breakStart(b.getBreakStart())
                .breakEnd(b.getBreakEnd())
                .breakType(b.getBreakType().name())
                .label(b.getLabel())
                .build();
    }
}
