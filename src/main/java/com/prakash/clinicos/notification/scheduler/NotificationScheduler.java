package com.prakash.clinicos.notification.scheduler;

import com.prakash.clinicos.appointment.entity.Appointment;
import com.prakash.clinicos.appointment.repository.AppointmentRepository;
import com.prakash.clinicos.doctor.entity.Doctor;
import com.prakash.clinicos.doctor.repository.DoctorRepository;
import com.prakash.clinicos.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sends 24-hour appointment reminder notifications every day at 08:00.
 *
 * Finds all PENDING/CONFIRMED appointments scheduled for tomorrow and
 * calls notifyAppointmentReminder() for each. Notification failures
 * are already swallowed inside NotificationService — this method
 * never throws.
 */
@Component
@Slf4j
public class NotificationScheduler {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final NotificationService notificationService;

    public NotificationScheduler(AppointmentRepository appointmentRepository,
                                  DoctorRepository doctorRepository,
                                  NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Appointment> appointments = appointmentRepository.findPendingOrConfirmedByDate(tomorrow);

        if (appointments.isEmpty()) {
            log.info("No appointments found for {} — skipping reminders", tomorrow);
            return;
        }

        log.info("Sending 24h reminders for {} appointments on {}", appointments.size(), tomorrow);

        // Build doctor name cache to avoid N+1 queries
        Map<Long, String> doctorNames = appointments.stream()
                .map(Appointment::getDoctorId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> doctorRepository.findByIdAndDeletedFalse(id)
                                .map(Doctor::getFullName)
                                .orElse("your doctor")
                ));

        for (Appointment appt : appointments) {
            notificationService.notifyAppointmentReminder(
                    appt.getClinicId(),
                    appt.getId(),
                    appt.getPatientId(),
                    doctorNames.getOrDefault(appt.getDoctorId(), "your doctor"),
                    appt.getAppointmentDate(),
                    appt.getStartTime()
            );
        }

        log.info("24h reminder run complete for {}", tomorrow);
    }
}
