package com.prakash.clinicos.doctor.service;

import com.prakash.clinicos.appointment.entity.Appointment;
import com.prakash.clinicos.appointment.repository.AppointmentRepository;
import com.prakash.clinicos.clinic.entity.Clinic;
import com.prakash.clinicos.clinic.entity.ClinicBusinessHours;
import com.prakash.clinicos.clinic.repository.ClinicBusinessHoursRepository;
import com.prakash.clinicos.clinic.repository.ClinicClosureDateRepository;
import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.doctor.dto.response.DoctorAvailabilityResponse;
import com.prakash.clinicos.doctor.entity.*;
import com.prakash.clinicos.doctor.repository.*;
import com.prakash.clinicos.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Computes the available 10-minute appointment slots for a doctor on a specific date.
 *
 * Algorithm (in order):
 *   1. Doctor must be active and not deleted.
 *   2. Clinic must not be deleted or emergency-closed.
 *   3. Date must not be a clinic closure date.
 *   4. Date must not be a doctor leave date.
 *   5. Check for a day override (DAY_OFF → no slots; LATE_START/EARLY_END/CUSTOM_HOURS → adjust window).
 *   6. Get doctor's weekly schedule for that day of week.
 *   7. Apply override to the schedule windows.
 *   8. If clinic is not 24/7: intersect doctor windows with clinic open hours.
 *   9. Load recurring breaks for that day.
 *  10. Generate 10-minute slots within each effective window, skipping break periods.
 *  11. Return the result.
 *
 * This service is read-only. All writes go through DoctorService or DoctorScheduleService.
 */
@Service
@Transactional(readOnly = true)
public class DoctorAvailabilityService {

    private final DoctorRepository doctorRepository;
    private final DoctorWeeklyScheduleRepository scheduleRepository;
    private final DoctorBreakRepository breakRepository;
    private final DoctorDayOverrideRepository overrideRepository;
    private final DoctorLeaveDateRepository leaveRepository;
    private final ClinicRepository clinicRepository;
    private final ClinicClosureDateRepository closureDateRepository;
    private final ClinicBusinessHoursRepository clinicBusinessHoursRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorAvailabilityService(DoctorRepository doctorRepository,
                                     DoctorWeeklyScheduleRepository scheduleRepository,
                                     DoctorBreakRepository breakRepository,
                                     DoctorDayOverrideRepository overrideRepository,
                                     DoctorLeaveDateRepository leaveRepository,
                                     ClinicRepository clinicRepository,
                                     ClinicClosureDateRepository closureDateRepository,
                                     ClinicBusinessHoursRepository clinicBusinessHoursRepository,
                                     AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
        this.breakRepository = breakRepository;
        this.overrideRepository = overrideRepository;
        this.leaveRepository = leaveRepository;
        this.clinicRepository = clinicRepository;
        this.closureDateRepository = closureDateRepository;
        this.clinicBusinessHoursRepository = clinicBusinessHoursRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public DoctorAvailabilityResponse computeAvailability(Long doctorId, LocalDate date) {

        // ── Step 1: Load doctor ───────────────────────────────────────────────
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(doctorId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Doctor not found with id: " + doctorId));

        if (!doctor.isActive()) {
            return unavailable(doctor, date, "DOCTOR_INACTIVE");
        }

        // ── Step 2: Load and validate clinic ─────────────────────────────────
        Clinic clinic = clinicRepository.findByIdAndDeletedFalse(doctor.getClinicId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Clinic not found for this doctor"));

        if (clinic.isEmergencyClosed()) {
            return unavailable(doctor, date, "CLINIC_CLOSED");
        }

        // ── Step 3: Clinic closure date ───────────────────────────────────────
        if (closureDateRepository.existsByClinicIdAndClosureDate(clinic.getId(), date)) {
            return unavailable(doctor, date, "CLINIC_CLOSED");
        }

        // ── Step 4: Doctor leave ──────────────────────────────────────────────
        if (leaveRepository.existsByDoctorIdAndLeaveDate(doctorId, date)) {
            return unavailable(doctor, date, "DOCTOR_ON_LEAVE");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // ── Step 5: Day override ──────────────────────────────────────────────
        Optional<DoctorDayOverride> overrideOpt =
                overrideRepository.findByDoctorIdAndOverrideDate(doctorId, date);

        if (overrideOpt.isPresent() && overrideOpt.get().getOverrideType() == OverrideType.DAY_OFF) {
            return unavailable(doctor, date, "DAY_OVERRIDE_DAY_OFF");
        }

        // ── Step 6: Weekly schedule for this day ──────────────────────────────
        List<DoctorWeeklySchedule> weeklyShifts =
                scheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);

        // Handle CUSTOM_HOURS override: weekly schedule is completely replaced
        DoctorDayOverride override = overrideOpt.orElse(null);
        List<TimeWindow> workingWindows;

        if (override != null && override.getOverrideType() == OverrideType.CUSTOM_HOURS) {
            // CUSTOM_HOURS replaces weekly schedule entirely
            workingWindows = List.of(new TimeWindow(override.getStartTime(), override.getEndTime()));
        } else if (weeklyShifts.isEmpty() && override == null) {
            // No schedule for this day, no override
            return unavailable(doctor, date, "NO_SCHEDULE_FOR_DAY");
        } else if (weeklyShifts.isEmpty()) {
            // Override exists but not CUSTOM_HOURS; no base schedule to adjust
            return unavailable(doctor, date, "NO_SCHEDULE_FOR_DAY");
        } else {
            // Build base windows from weekly schedule
            workingWindows = weeklyShifts.stream()
                    .map(s -> new TimeWindow(s.getStartTime(), s.getEndTime()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // ── Step 7: Apply override adjustments ───────────────────────────────
        boolean overrideApplied = override != null
                && override.getOverrideType() != OverrideType.CUSTOM_HOURS;
        String overrideTypeName = override != null ? override.getOverrideType().name() : null;

        if (override != null) {
            if (override.getOverrideType() == OverrideType.LATE_START) {
                LocalTime newStart = override.getStartTime();
                // Round up to nearest 10-minute boundary (in case admin entered e.g. 09:17)
                newStart = roundUpToTenMinutes(newStart);
                workingWindows = applyLateStart(workingWindows, newStart);
            } else if (override.getOverrideType() == OverrideType.EARLY_END) {
                LocalTime newEnd = override.getEndTime();
                // Round down to nearest 10-minute boundary
                newEnd = roundDownToTenMinutes(newEnd);
                workingWindows = applyEarlyEnd(workingWindows, newEnd);
            }
            // CUSTOM_HOURS already handled above; DAY_OFF handled in Step 5
        }

        // Remove empty windows (e.g. if late start pushed past end time)
        workingWindows = workingWindows.stream()
                .filter(w -> w.start().isBefore(w.end()))
                .toList();

        if (workingWindows.isEmpty()) {
            return unavailable(doctor, date, "NO_SCHEDULE_FOR_DAY");
        }

        // ── Step 8: Intersect with clinic open hours ──────────────────────────
        if (!clinic.isAlwaysOpen()) {
            List<ClinicBusinessHours> clinicHours =
                    clinicBusinessHoursRepository.findByClinicIdAndDayOfWeek(clinic.getId(), dayOfWeek);

            if (!clinicHours.isEmpty()) {
                workingWindows = intersectWithClinic(workingWindows, clinicHours);
                if (workingWindows.isEmpty()) {
                    return unavailable(doctor, date, "CLINIC_CLOSED");
                }
            }
            // If clinic has no hours for this day AND is not 24/7, clinic is closed
            if (clinicHours.isEmpty()) {
                return unavailable(doctor, date, "CLINIC_CLOSED");
            }
        }

        // ── Step 9: Load recurring breaks ─────────────────────────────────────
        List<DoctorBreak> breaks =
                breakRepository.findByDoctorIdAndDayOfWeekOrderByBreakStartAsc(doctorId, dayOfWeek);

        // ── Step 10: Generate 10-minute slots, skipping breaks and booked appts
        List<Appointment> bookedAppointments =
                appointmentRepository.findActiveByDoctorAndDate(doctorId, date);
        List<String> slots = generateSlots(workingWindows, breaks, bookedAppointments);

        // ── Step 11: Build response ───────────────────────────────────────────
        List<DoctorAvailabilityResponse.WorkingWindow> windowResponses = workingWindows.stream()
                .map(w -> DoctorAvailabilityResponse.WorkingWindow.builder()
                        .effectiveStart(w.start())
                        .effectiveEnd(w.end())
                        .overrideApplied(overrideApplied)
                        .overrideType(overrideTypeName)
                        .build())
                .toList();

        List<DoctorAvailabilityResponse.BreakInfo> breakInfos = breaks.stream()
                .map(b -> DoctorAvailabilityResponse.BreakInfo.builder()
                        .breakStart(b.getBreakStart())
                        .breakEnd(b.getBreakEnd())
                        .breakType(b.getBreakType().name())
                        .label(b.getLabel())
                        .build())
                .toList();

        return DoctorAvailabilityResponse.builder()
                .doctorId(doctorId)
                .doctorName(doctor.getFullName())
                .clinicId(doctor.getClinicId())
                .date(date)
                .dayOfWeek(dayOfWeek)
                .available(!slots.isEmpty())
                .unavailableReason(slots.isEmpty() ? "NO_SCHEDULE_FOR_DAY" : null)
                .workingWindows(windowResponses)
                .breaks(breakInfos.isEmpty() ? null : breakInfos)
                .slots(slots)
                .totalSlots(slots.size())
                .build();
    }

    // ── Algorithm helpers ─────────────────────────────────────────────────────

    /**
     * Applies a LATE_START override: pushes the start of any overlapping window forward.
     * If the new start is past a window's end, that window is discarded.
     */
    private List<TimeWindow> applyLateStart(List<TimeWindow> windows, LocalTime newStart) {
        return windows.stream()
                .map(w -> {
                    if (newStart.isAfter(w.start())) {
                        return new TimeWindow(newStart, w.end());
                    }
                    return w;
                })
                .filter(w -> w.start().isBefore(w.end()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Applies an EARLY_END override: truncates any window that extends past the new end.
     */
    private List<TimeWindow> applyEarlyEnd(List<TimeWindow> windows, LocalTime newEnd) {
        return windows.stream()
                .map(w -> {
                    if (newEnd.isBefore(w.end())) {
                        return new TimeWindow(w.start(), newEnd);
                    }
                    return w;
                })
                .filter(w -> w.start().isBefore(w.end()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Intersects doctor working windows with clinic open hours.
     * Each doctor window is clipped to the union of clinic shift windows that overlap with it.
     * Resulting segments shorter than 10 minutes are discarded.
     */
    private List<TimeWindow> intersectWithClinic(List<TimeWindow> doctorWindows,
                                                  List<ClinicBusinessHours> clinicHours) {
        List<TimeWindow> result = new ArrayList<>();
        for (TimeWindow dw : doctorWindows) {
            for (ClinicBusinessHours ch : clinicHours) {
                LocalTime intersectStart = dw.start().isAfter(ch.getOpenTime())
                        ? dw.start() : ch.getOpenTime();
                LocalTime intersectEnd = dw.end().isBefore(ch.getCloseTime())
                        ? dw.end() : ch.getCloseTime();
                if (intersectStart.isBefore(intersectEnd)
                        && intersectEnd.minusMinutes(10).compareTo(intersectStart) >= 0) {
                    result.add(new TimeWindow(intersectStart, intersectEnd));
                }
            }
        }
        return result;
    }

    /**
     * Generates 10-minute slot start times within the working windows,
     * excluding any time that overlaps a break or an existing booked appointment.
     *
     * Overlap condition for a slot [T, T+10):
     *   - Break:       T < breakEnd       AND T+10 > breakStart
     *   - Appointment: T < appt.endTime   AND T+10 > appt.startTime
     *
     * This is the standard interval overlap test: [s1,e1) overlaps [s2,e2) iff s1 < e2 AND e1 > s2.
     */
    private List<String> generateSlots(List<TimeWindow> windows,
                                        List<DoctorBreak> breaks,
                                        List<Appointment> bookedAppointments) {
        List<String> slots = new ArrayList<>();

        for (TimeWindow window : windows) {
            LocalTime cursor = window.start();
            LocalTime windowEnd = window.end();

            while (!cursor.plusMinutes(10).isAfter(windowEnd)) {
                final LocalTime slotStart = cursor;
                final LocalTime slotEnd = cursor.plusMinutes(10);

                boolean duringBreak = breaks.stream().anyMatch(b ->
                        slotStart.isBefore(b.getBreakEnd()) && slotEnd.isAfter(b.getBreakStart()));

                boolean duringAppointment = bookedAppointments.stream().anyMatch(a ->
                        slotStart.isBefore(a.getEndTime()) && slotEnd.isAfter(a.getStartTime()));

                if (!duringBreak && !duringAppointment) {
                    slots.add(String.format("%02d:%02d", slotStart.getHour(), slotStart.getMinute()));
                }

                cursor = cursor.plusMinutes(10);
            }
        }

        return slots;
    }

    /** Round a time UP to the nearest 10-minute boundary. */
    private LocalTime roundUpToTenMinutes(LocalTime time) {
        int minute = time.getMinute();
        int remainder = minute % 10;
        if (remainder == 0) return time;
        int minutesToAdd = 10 - remainder;
        return time.plusMinutes(minutesToAdd).withSecond(0).withNano(0);
    }

    /** Round a time DOWN to the nearest 10-minute boundary. */
    private LocalTime roundDownToTenMinutes(LocalTime time) {
        int minute = time.getMinute();
        int remainder = minute % 10;
        return time.minusMinutes(remainder).withSecond(0).withNano(0);
    }

    private DoctorAvailabilityResponse unavailable(Doctor doctor, LocalDate date, String reason) {
        return DoctorAvailabilityResponse.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getFullName())
                .clinicId(doctor.getClinicId())
                .date(date)
                .dayOfWeek(date.getDayOfWeek())
                .available(false)
                .unavailableReason(reason)
                .slots(List.of())
                .totalSlots(0)
                .build();
    }

    /** Immutable pair of (start, end) times representing one working window. */
    private record TimeWindow(LocalTime start, LocalTime end) {}
}
