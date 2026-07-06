-- V1__baseline.sql
-- Phase 1: Baseline migration
-- This file runs once on first startup and establishes Flyway's version tracking.
-- Actual table creation will begin from V2 onwards (auth module).

-- Flyway creates its own 'flyway_schema_history' table automatically to track
-- which migrations have been applied. You will see it in your DB after first run.
