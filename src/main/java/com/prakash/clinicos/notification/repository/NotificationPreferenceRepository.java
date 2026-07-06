package com.prakash.clinicos.notification.repository;

import com.prakash.clinicos.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByClinicId(Long clinicId);
}
