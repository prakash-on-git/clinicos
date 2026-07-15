package com.prakash.clinicos.billing.cobol;

/**
 * Thrown when the compiled GnuCOBOL billing binary is missing, not
 * executable, or fails during invocation. BillingService catches this and
 * falls back to the equivalent Java BigDecimal calculation, so a machine
 * without GnuCOBOL installed still issues correct invoices.
 */
public class CobolUnavailableException extends RuntimeException {
    public CobolUnavailableException(String message) {
        super(message);
    }

    public CobolUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
