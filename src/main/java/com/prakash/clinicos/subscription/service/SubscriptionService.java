package com.prakash.clinicos.subscription.service;

import com.prakash.clinicos.doctor.repository.DoctorRepository;
import com.prakash.clinicos.exception.AppException;
import com.prakash.clinicos.patient.repository.PatientRepository;
import com.prakash.clinicos.security.UserPrincipal;
import com.prakash.clinicos.subscription.dto.request.SubscribeRequest;
import com.prakash.clinicos.subscription.dto.response.PlanResponse;
import com.prakash.clinicos.subscription.dto.response.SubscriptionResponse;
import com.prakash.clinicos.subscription.entity.ClinicSubscription;
import com.prakash.clinicos.subscription.entity.Plan;
import com.prakash.clinicos.subscription.entity.PlanTier;
import com.prakash.clinicos.subscription.entity.SubscriptionStatus;
import com.prakash.clinicos.subscription.repository.ClinicSubscriptionRepository;
import com.prakash.clinicos.subscription.repository.PlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SubscriptionService {

    private final PlanRepository planRepository;
    private final ClinicSubscriptionRepository clinicSubscriptionRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public SubscriptionService(PlanRepository planRepository,
                               ClinicSubscriptionRepository clinicSubscriptionRepository,
                               DoctorRepository doctorRepository,
                               PatientRepository patientRepository) {
        this.planRepository = planRepository;
        this.clinicSubscriptionRepository = clinicSubscriptionRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PlanResponse> getPlans() {
        return planRepository.findByActiveTrue()
                .stream()
                .map(this::toPlanResponse)
                .toList();
    }

    @Transactional
    public SubscriptionResponse getSubscription(Long clinicId) {
        ClinicSubscription sub = loadOrCreateSub(clinicId);
        Plan plan = planRepository.findById(sub.getPlanId())
                .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Plan not found for subscription"));
        return toResponse(sub, plan);
    }

    @Transactional
    public SubscriptionResponse subscribe(Long clinicId, SubscribeRequest req, UserPrincipal principal) {
        Plan plan = planRepository.findByTier(req.getTier())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Plan not found for tier: " + req.getTier()));

        ClinicSubscription sub = clinicSubscriptionRepository.findByClinicId(clinicId)
                .orElse(null);

        if (sub != null) {
            // Update existing subscription
            sub.setPlanId(plan.getId());
            sub.setStatus(SubscriptionStatus.ACTIVE);
            sub.setStartedAt(LocalDateTime.now());
            sub.setNextBillingDate(LocalDate.now().plusMonths(1));
            sub.setCancelledAt(null);
        } else {
            // Create new subscription
            sub = ClinicSubscription.builder()
                    .clinicId(clinicId)
                    .planId(plan.getId())
                    .status(SubscriptionStatus.ACTIVE)
                    .startedAt(LocalDateTime.now())
                    .nextBillingDate(LocalDate.now().plusMonths(1))
                    .build();
        }

        sub = clinicSubscriptionRepository.save(sub);
        log.info("Clinic {} subscribed to plan {} by user {}",
                clinicId, plan.getTier(), principal.getEmail());
        return toResponse(sub, plan);
    }

    /**
     * Called from ClinicService.createClinic() after saving the clinic.
     * Auto-assigns the FREE plan if no subscription exists yet.
     */
    @Transactional
    public void autoAssignFree(Long clinicId) {
        if (clinicSubscriptionRepository.findByClinicId(clinicId).isPresent()) {
            return; // already has a subscription
        }
        Plan freePlan = planRepository.findByTier(PlanTier.FREE)
                .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "FREE plan not found in database. Please run V12 migration."));

        ClinicSubscription sub = ClinicSubscription.builder()
                .clinicId(clinicId)
                .planId(freePlan.getId())
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .build();

        clinicSubscriptionRepository.save(sub);
        log.info("Auto-assigned FREE plan to clinic {}", clinicId);
    }

    /**
     * Enforces the doctor limit for the clinic's current plan.
     * Throws 402 PAYMENT_REQUIRED if at or above the plan's max doctor count.
     * Logs a warning when >= 80% of the limit is reached.
     */
    @Transactional(readOnly = true)
    public void enforceDoctorLimit(Long clinicId) {
        ClinicSubscription sub = clinicSubscriptionRepository.findByClinicId(clinicId)
                .orElse(null);
        if (sub == null) return; // no subscription = no limit enforced

        Plan plan = planRepository.findById(sub.getPlanId()).orElse(null);
        if (plan == null) return;

        long currentDoctors = doctorRepository.countByClinicIdAndDeletedFalse(clinicId);
        if (plan.getMaxDoctors() != null) {
            if (currentDoctors >= plan.getMaxDoctors()) {
                throw new AppException(HttpStatus.PAYMENT_REQUIRED,
                        "Doctor limit reached for your " + plan.getTier() + " plan (" +
                        plan.getMaxDoctors() + " doctors). Please upgrade to add more doctors.");
            }
            if (currentDoctors >= plan.getMaxDoctors() * 0.8) {
                log.warn("Clinic {} is approaching doctor limit: {}/{}", clinicId, currentDoctors, plan.getMaxDoctors());
            }
        }
    }

    /**
     * Enforces the monthly patient registration limit for the clinic's current plan.
     * Throws 402 PAYMENT_REQUIRED if at or above the plan's monthly patient limit.
     * Logs a warning when >= 80% of the limit is reached.
     */
    @Transactional(readOnly = true)
    public void enforcePatientMonthlyLimit(Long clinicId) {
        ClinicSubscription sub = clinicSubscriptionRepository.findByClinicId(clinicId)
                .orElse(null);
        if (sub == null) return; // no subscription = no limit enforced

        Plan plan = planRepository.findById(sub.getPlanId()).orElse(null);
        if (plan == null) return;

        LocalDate now = LocalDate.now();
        LocalDateTime from = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime to = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        long currentPatients = patientRepository.countNewRegistrations(clinicId, from, to);

        if (plan.getMaxPatientsPerMonth() != null) {
            if (currentPatients >= plan.getMaxPatientsPerMonth()) {
                throw new AppException(HttpStatus.PAYMENT_REQUIRED,
                        "Monthly patient registration limit reached for your " + plan.getTier() + " plan (" +
                        plan.getMaxPatientsPerMonth() + "/month). Please upgrade.");
            }
            if (currentPatients >= plan.getMaxPatientsPerMonth() * 0.8) {
                log.warn("Clinic {} is approaching patient registration limit: {}/{}", clinicId, currentPatients, plan.getMaxPatientsPerMonth());
            }
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Loads an existing subscription or creates a FREE one on demand.
     */
    private ClinicSubscription loadOrCreateSub(Long clinicId) {
        return clinicSubscriptionRepository.findByClinicId(clinicId)
                .orElseGet(() -> {
                    Plan freePlan = planRepository.findByTier(PlanTier.FREE)
                            .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    "FREE plan not found in database. Please run V12 migration."));
                    ClinicSubscription sub = ClinicSubscription.builder()
                            .clinicId(clinicId)
                            .planId(freePlan.getId())
                            .status(SubscriptionStatus.ACTIVE)
                            .startedAt(LocalDateTime.now())
                            .build();
                    return clinicSubscriptionRepository.save(sub);
                });
    }

    private PlanResponse toPlanResponse(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .tier(plan.getTier())
                .displayName(plan.getDisplayName())
                .maxDoctors(plan.getMaxDoctors())
                .maxPatientsPerMonth(plan.getMaxPatientsPerMonth())
                .priceMonthly(plan.getPriceMonthly())
                .build();
    }

    private SubscriptionResponse toResponse(ClinicSubscription sub, Plan plan) {
        return SubscriptionResponse.builder()
                .id(sub.getId())
                .clinicId(sub.getClinicId())
                .plan(toPlanResponse(plan))
                .status(sub.getStatus())
                .startedAt(sub.getStartedAt())
                .expiresAt(sub.getExpiresAt())
                .nextBillingDate(sub.getNextBillingDate())
                .build();
    }
}
