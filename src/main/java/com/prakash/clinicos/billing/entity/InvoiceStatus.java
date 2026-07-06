package com.prakash.clinicos.billing.entity;

public enum InvoiceStatus {
    /** Created but not yet sent to the patient. Items can still be added/removed. */
    DRAFT,

    /** Finalized and presented to the patient. No further edits allowed. */
    ISSUED,

    /** Fully paid. amount_due = 0. */
    PAID,

    /** At least one payment received but amount_due > 0. */
    PARTIALLY_PAID,

    /** Voided — replaced, duplicate, or patient not seen. */
    CANCELLED,

    /** Was PAID/PARTIALLY_PAID, now refunded. */
    REFUNDED
}
