package com.prakash.clinicos.subscription.repository;

import com.prakash.clinicos.subscription.entity.ClinicSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClinicSubscriptionRepository extends JpaRepository<ClinicSubscription, Long> {

    Optional<ClinicSubscription> findByClinicId(Long clinicId);

    List<ClinicSubscription> findByClinicIdIn(List<Long> clinicIds);

    // For revenue calculation: count active subs per plan
    @Query("SELECT cs.planId, COUNT(cs) FROM ClinicSubscription cs WHERE cs.status = 'ACTIVE' GROUP BY cs.planId")
    List<Object[]> countActiveByPlan();
}
