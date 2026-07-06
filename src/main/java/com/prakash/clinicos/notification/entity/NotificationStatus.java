package com.prakash.clinicos.notification.entity;

public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,

    /** No contact info available or preference disabled — intentionally not sent. */
    SKIPPED
}
