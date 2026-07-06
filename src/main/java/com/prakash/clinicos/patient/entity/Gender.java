package com.prakash.clinicos.patient.entity;

/**
 * Patient gender options.
 *
 * Stored as a string column in the DB (not a PostgreSQL enum type) so we can add
 * values without a schema migration. The application validates via @Pattern on the DTO.
 */
public enum Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY
}
