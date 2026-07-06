package com.prakash.clinicos.notification.repository;

import com.prakash.clinicos.notification.entity.NotificationChannel;
import com.prakash.clinicos.notification.entity.NotificationLog;
import com.prakash.clinicos.notification.entity.NotificationStatus;
import com.prakash.clinicos.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    @Query("""
            SELECT n FROM NotificationLog n
            WHERE n.clinicId = :clinicId
              AND (:type    IS NULL OR n.type    = :type)
              AND (:channel IS NULL OR n.channel = :channel)
              AND (:status  IS NULL OR n.status  = :status)
              AND (:from    IS NULL OR n.createdAt >= :from)
              AND (:to      IS NULL OR n.createdAt <= :to)
            ORDER BY n.createdAt DESC
            """)
    Page<NotificationLog> search(
            @Param("clinicId") Long clinicId,
            @Param("type")     NotificationType type,
            @Param("channel")  NotificationChannel channel,
            @Param("status")   NotificationStatus status,
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to,
            Pageable pageable);
}
