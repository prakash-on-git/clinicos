package com.prakash.clinicos.reporting.service;

import com.prakash.clinicos.appointment.entity.AppointmentStatus;
import com.prakash.clinicos.appointment.repository.AppointmentRepository;
import com.prakash.clinicos.billing.entity.InvoiceStatus;
import com.prakash.clinicos.billing.entity.PaymentMethod;
import com.prakash.clinicos.billing.repository.InvoiceRepository;
import com.prakash.clinicos.billing.repository.PaymentRepository;
import com.prakash.clinicos.clinic.repository.ClinicRepository;
import com.prakash.clinicos.doctor.entity.Doctor;
import com.prakash.clinicos.doctor.repository.DoctorRepository;
import com.prakash.clinicos.doctor.repository.TreatmentTypeRepository;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.queue.entity.QueueStatus;
import com.prakash.clinicos.queue.repository.QueueTokenRepository;
import com.prakash.clinicos.reporting.dto.response.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportingService {

    private static final Set<InvoiceStatus> EXCLUDED_FROM_REVENUE =
            Set.of(InvoiceStatus.CANCELLED, InvoiceStatus.REFUNDED);

    private final ClinicRepository clinicRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final QueueTokenRepository queueRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TreatmentTypeRepository treatmentTypeRepository;

    public ReportingService(ClinicRepository clinicRepository,
                            InvoiceRepository invoiceRepository,
                            PaymentRepository paymentRepository,
                            AppointmentRepository appointmentRepository,
                            QueueTokenRepository queueRepository,
                            PatientRepository patientRepository,
                            DoctorRepository doctorRepository,
                            TreatmentTypeRepository treatmentTypeRepository) {
        this.clinicRepository = clinicRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
        this.queueRepository = queueRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.treatmentTypeRepository = treatmentTypeRepository;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Revenue report
    // ════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public RevenueReportResponse revenueReport(Long clinicId, LocalDate from, LocalDate to) {
        assertClinic(clinicId);

        Object[] summary = invoiceRepository.revenueSummary(clinicId, from, to, EXCLUDED_FROM_REVENUE);
        BigDecimal totalInvoiced  = toBigDecimal(summary[0]);
        BigDecimal totalCollected = toBigDecimal(summary[1]);
        BigDecimal totalOutstanding = toBigDecimal(summary[2]);
        long invoiceCount = toLong(summary[3]);

        BigDecimal totalRefunded = invoiceRepository.totalByStatus(clinicId, from, to, InvoiceStatus.REFUNDED);

        // By doctor
        Map<Long, Doctor> doctorCache = buildDoctorCache(clinicId);
        List<Object[]> byDoctorRows = invoiceRepository.revenueByDoctor(clinicId, from, to, EXCLUDED_FROM_REVENUE);
        List<RevenueReportResponse.DoctorRevenue> byDoctor = byDoctorRows.stream()
                .map(r -> RevenueReportResponse.DoctorRevenue.builder()
                        .doctorId(toLong(r[0]))
                        .doctorName(doctorDisplayName(doctorCache.get(toLong(r[0]))))
                        .invoiced(toBigDecimal(r[1]))
                        .collected(toBigDecimal(r[2]))
                        .build())
                .toList();

        // By payment method
        List<Object[]> methodRows = paymentRepository.amountByPaymentMethod(clinicId, from, to);
        Map<String, BigDecimal> byPaymentMethod = new LinkedHashMap<>();
        for (Object[] r : methodRows) {
            String method = r[0] instanceof PaymentMethod pm ? pm.name() : String.valueOf(r[0]);
            byPaymentMethod.put(method, toBigDecimal(r[1]));
        }

        return RevenueReportResponse.builder()
                .fromDate(from)
                .toDate(to)
                .totalInvoiced(totalInvoiced)
                .totalCollected(totalCollected)
                .totalOutstanding(totalOutstanding)
                .totalRefunded(totalRefunded)
                .invoiceCount(invoiceCount)
                .byDoctor(byDoctor)
                .byPaymentMethod(byPaymentMethod)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Appointment report
    // ════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public AppointmentReportResponse appointmentReport(Long clinicId, LocalDate from, LocalDate to) {
        assertClinic(clinicId);

        // Aggregate overall counts
        List<Object[]> statusRows = appointmentRepository.countByStatusForRange(clinicId, from, to);
        Map<AppointmentStatus, Long> counts = new EnumMap<>(AppointmentStatus.class);
        for (Object[] r : statusRows) {
            counts.put((AppointmentStatus) r[0], toLong(r[1]));
        }

        long completed   = counts.getOrDefault(AppointmentStatus.COMPLETED, 0L);
        long confirmed   = counts.getOrDefault(AppointmentStatus.CONFIRMED, 0L);
        long pending     = counts.getOrDefault(AppointmentStatus.PENDING, 0L);
        long inProgress  = counts.getOrDefault(AppointmentStatus.IN_PROGRESS, 0L);
        long cancelled   = counts.getOrDefault(AppointmentStatus.CANCELLED, 0L);
        long noShow      = counts.getOrDefault(AppointmentStatus.NO_SHOW, 0L);
        long rescheduled = counts.getOrDefault(AppointmentStatus.RESCHEDULED, 0L);
        long total       = counts.values().stream().mapToLong(Long::longValue).sum();

        // Rescheduled appointments produce a new record — exclude from rate denominator
        long denominator = total - rescheduled;
        double completionRate = denominator > 0
                ? Math.round((completed * 1000.0 / denominator)) / 10.0
                : 0.0;

        // By doctor
        Map<Long, Doctor> doctorCache = buildDoctorCache(clinicId);
        List<Object[]> byDoctorRows = appointmentRepository.countByDoctorAndStatusForRange(clinicId, from, to);

        // Group by doctorId
        Map<Long, Map<AppointmentStatus, Long>> byDoctorMap = new LinkedHashMap<>();
        for (Object[] r : byDoctorRows) {
            Long doctorId = toLong(r[0]);
            AppointmentStatus status = (AppointmentStatus) r[1];
            long count = toLong(r[2]);
            byDoctorMap.computeIfAbsent(doctorId, k -> new EnumMap<>(AppointmentStatus.class))
                    .put(status, count);
        }

        List<AppointmentReportResponse.DoctorAppointments> byDoctor = byDoctorMap.entrySet().stream()
                .map(e -> {
                    Map<AppointmentStatus, Long> dc = e.getValue();
                    long dTotal = dc.values().stream().mapToLong(Long::longValue).sum();
                    return AppointmentReportResponse.DoctorAppointments.builder()
                            .doctorId(e.getKey())
                            .doctorName(doctorDisplayName(doctorCache.get(e.getKey())))
                            .total(dTotal)
                            .completed(dc.getOrDefault(AppointmentStatus.COMPLETED, 0L))
                            .cancelled(dc.getOrDefault(AppointmentStatus.CANCELLED, 0L))
                            .noShow(dc.getOrDefault(AppointmentStatus.NO_SHOW, 0L))
                            .build();
                })
                .toList();

        return AppointmentReportResponse.builder()
                .fromDate(from)
                .toDate(to)
                .total(total)
                .completed(completed)
                .confirmed(confirmed)
                .pending(pending)
                .inProgress(inProgress)
                .cancelled(cancelled)
                .noShow(noShow)
                .rescheduled(rescheduled)
                .completionRate(completionRate)
                .byDoctor(byDoctor)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Queue report
    // ════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public QueueReportResponse queueReport(Long clinicId, LocalDate from, LocalDate to) {
        assertClinic(clinicId);

        // Overall counts
        List<Object[]> statusRows = queueRepository.countByStatusForRange(clinicId, from, to);
        Map<QueueStatus, Long> counts = new EnumMap<>(QueueStatus.class);
        for (Object[] r : statusRows) {
            counts.put((QueueStatus) r[0], toLong(r[1]));
        }

        long completed  = counts.getOrDefault(QueueStatus.COMPLETED, 0L);
        long skipped    = counts.getOrDefault(QueueStatus.SKIPPED, 0L);
        long cancelled  = counts.getOrDefault(QueueStatus.CANCELLED, 0L);
        long waiting    = counts.getOrDefault(QueueStatus.WAITING, 0L);
        long called     = counts.getOrDefault(QueueStatus.CALLED, 0L);
        long inProgress = counts.getOrDefault(QueueStatus.IN_PROGRESS, 0L);
        long total      = counts.values().stream().mapToLong(Long::longValue).sum();

        double skipRate = total > 0
                ? Math.round((skipped * 1000.0 / total)) / 10.0
                : 0.0;

        // Time averages — computed in Java from timestamp rows
        List<Object[]> timestamps = queueRepository.completedTokenTimestamps(clinicId, from, to);
        double avgWait  = averageWaitMinutes(timestamps);
        double avgConsult = averageConsultMinutes(timestamps);

        // By doctor
        Map<Long, Doctor> doctorCache = buildDoctorCache(clinicId);
        List<Object[]> byDoctorRows = queueRepository.countByDoctorAndStatusForRange(clinicId, from, to);

        Map<Long, Map<QueueStatus, Long>> byDoctorMap = new LinkedHashMap<>();
        for (Object[] r : byDoctorRows) {
            Long doctorId = toLong(r[0]);
            QueueStatus status = (QueueStatus) r[1];
            long count = toLong(r[2]);
            byDoctorMap.computeIfAbsent(doctorId, k -> new EnumMap<>(QueueStatus.class))
                    .put(status, count);
        }

        // Group timestamps by doctor for per-doctor averages
        Map<Long, List<Object[]>> tsByDoctor = timestamps.stream()
                .collect(Collectors.groupingBy(r -> toLong(r[0])));

        List<QueueReportResponse.DoctorQueue> byDoctor = byDoctorMap.entrySet().stream()
                .map(e -> {
                    Map<QueueStatus, Long> dc = e.getValue();
                    long dTotal = dc.values().stream().mapToLong(Long::longValue).sum();
                    List<Object[]> dTs = tsByDoctor.getOrDefault(e.getKey(), List.of());
                    return QueueReportResponse.DoctorQueue.builder()
                            .doctorId(e.getKey())
                            .doctorName(doctorDisplayName(doctorCache.get(e.getKey())))
                            .total(dTotal)
                            .completed(dc.getOrDefault(QueueStatus.COMPLETED, 0L))
                            .skipped(dc.getOrDefault(QueueStatus.SKIPPED, 0L))
                            .avgWaitMinutes(averageWaitMinutes(dTs))
                            .avgConsultationMinutes(averageConsultMinutes(dTs))
                            .build();
                })
                .toList();

        return QueueReportResponse.builder()
                .fromDate(from)
                .toDate(to)
                .totalTokens(total)
                .completed(completed)
                .skipped(skipped)
                .cancelled(cancelled)
                .waiting(waiting)
                .called(called)
                .inProgress(inProgress)
                .averageWaitMinutes(avgWait)
                .averageConsultationMinutes(avgConsult)
                .skipRate(skipRate)
                .byDoctor(byDoctor)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Patient report
    // ════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public PatientReportResponse patientReport(Long clinicId, LocalDate from, LocalDate to) {
        assertClinic(clinicId);

        long totalActive = patientRepository.countByClinicIdAndDeletedFalseAndActiveTrue(clinicId);

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);
        long newRegistrations = patientRepository.countNewRegistrations(clinicId, fromDt, toDt);

        List<Object[]> genderRows = patientRepository.countByGender(clinicId);
        Map<String, Long> byGender = new LinkedHashMap<>();
        for (Object[] r : genderRows) {
            String key = r[0] == null ? "UNKNOWN" : r[0].toString();
            byGender.put(key, toLong(r[1]));
        }

        return PatientReportResponse.builder()
                .fromDate(from)
                .toDate(to)
                .totalActive(totalActive)
                .newRegistrations(newRegistrations)
                .byGender(byGender)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Doctor performance report
    // ════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public DoctorPerformanceResponse doctorPerformance(Long clinicId, Long doctorId,
                                                        LocalDate from, LocalDate to) {
        assertClinic(clinicId);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Doctor not found"));
        if (!doctor.getClinicId().equals(clinicId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Doctor does not belong to this clinic");
        }

        // Appointment counts
        List<Object[]> apptRows = appointmentRepository.countByDoctorAndStatusForRange(clinicId, from, to);
        Map<AppointmentStatus, Long> apptCounts = new EnumMap<>(AppointmentStatus.class);
        for (Object[] r : apptRows) {
            if (toLong(r[0]) == doctorId) {
                apptCounts.put((AppointmentStatus) r[1], toLong(r[2]));
            }
        }
        long apptTotal     = apptCounts.values().stream().mapToLong(Long::longValue).sum();
        long apptCompleted = apptCounts.getOrDefault(AppointmentStatus.COMPLETED, 0L);
        long apptCancelled = apptCounts.getOrDefault(AppointmentStatus.CANCELLED, 0L);
        long apptNoShow    = apptCounts.getOrDefault(AppointmentStatus.NO_SHOW, 0L);

        // Revenue
        List<Object[]> revRows = invoiceRepository.revenueByDoctor(clinicId, from, to, EXCLUDED_FROM_REVENUE);
        BigDecimal revenueGenerated = BigDecimal.ZERO;
        BigDecimal revenueCollected = BigDecimal.ZERO;
        for (Object[] r : revRows) {
            if (toLong(r[0]) == doctorId) {
                revenueGenerated = toBigDecimal(r[1]);
                revenueCollected = toBigDecimal(r[2]);
                break;
            }
        }

        // Queue averages
        List<Object[]> queueTs = queueRepository.completedTokenTimestampsByDoctor(clinicId, doctorId, from, to);
        long tokensCompleted = queueTs.size();
        double avgWait    = averageWaitMinutes(queueTs);
        double avgConsult = averageConsultMinutes(queueTs);

        // Top treatments (limit 5)
        List<Object[]> treatRows = appointmentRepository.topTreatmentsByDoctor(
                clinicId, doctorId, from, to, AppointmentStatus.COMPLETED);
        List<DoctorPerformanceResponse.TreatmentCount> topTreatments = treatRows.stream()
                .limit(5)
                .map(r -> {
                    Long treatId = toLong(r[0]);
                    String name = treatmentTypeRepository.findById(treatId)
                            .map(t -> t.getName())
                            .orElse("Unknown");
                    return DoctorPerformanceResponse.TreatmentCount.builder()
                            .treatmentTypeId(treatId)
                            .treatmentName(name)
                            .count(toLong(r[1]))
                            .build();
                })
                .toList();

        return DoctorPerformanceResponse.builder()
                .doctorId(doctorId)
                .doctorName("Dr. " + doctor.getFullName())
                .specialization(doctor.getSpecialization())
                .fromDate(from)
                .toDate(to)
                .appointmentsTotal(apptTotal)
                .appointmentsCompleted(apptCompleted)
                .appointmentsCancelled(apptCancelled)
                .appointmentsNoShow(apptNoShow)
                .revenueGenerated(revenueGenerated)
                .revenueCollected(revenueCollected)
                .tokensCompleted(tokensCompleted)
                .averageWaitMinutes(avgWait)
                .averageConsultationMinutes(avgConsult)
                .topTreatments(topTreatments)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════════════════════════════════

    private void assertClinic(Long clinicId) {
        clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Clinic not found"));
    }

    private Map<Long, Doctor> buildDoctorCache(Long clinicId) {
        return doctorRepository.findByClinicIdAndDeletedFalse(clinicId).stream()
                .collect(Collectors.toMap(Doctor::getId, d -> d));
    }

    private String doctorDisplayName(Doctor doctor) {
        return doctor != null ? "Dr. " + doctor.getFullName() : null;
    }

    /**
     * Average minutes from token creation → calledAt (patient wait time).
     * Row format: [doctorId, createdAt, calledAt, startedAt, completedAt]
     */
    private double averageWaitMinutes(List<Object[]> rows) {
        if (rows.isEmpty()) return 0.0;
        OptionalDouble avg = rows.stream()
                .mapToLong(r -> Duration.between((LocalDateTime) r[1], (LocalDateTime) r[2]).toMinutes())
                .filter(m -> m >= 0)
                .average();
        return avg.isPresent() ? Math.round(avg.getAsDouble() * 10.0) / 10.0 : 0.0;
    }

    /**
     * Average minutes from startedAt → completedAt (consultation duration).
     */
    private double averageConsultMinutes(List<Object[]> rows) {
        if (rows.isEmpty()) return 0.0;
        OptionalDouble avg = rows.stream()
                .mapToLong(r -> Duration.between((LocalDateTime) r[3], (LocalDateTime) r[4]).toMinutes())
                .filter(m -> m >= 0)
                .average();
        return avg.isPresent() ? Math.round(avg.getAsDouble() * 10.0) / 10.0 : 0.0;
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        return new BigDecimal(v.toString());
    }

    private long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Long l) return l;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }
}
