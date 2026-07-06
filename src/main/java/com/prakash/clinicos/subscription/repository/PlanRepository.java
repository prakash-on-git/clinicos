package com.prakash.clinicos.subscription.repository;

import com.prakash.clinicos.subscription.entity.Plan;
import com.prakash.clinicos.subscription.entity.PlanTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findByActiveTrue();

    Optional<Plan> findByTier(PlanTier tier);
}
