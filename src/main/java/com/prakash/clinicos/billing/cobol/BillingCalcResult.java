package com.prakash.clinicos.billing.cobol;

import java.math.BigDecimal;

/** Result of one COBOL billing-engine invocation: discount, tax, and total, each scale 2. */
public record BillingCalcResult(BigDecimal discountAmount, BigDecimal taxAmount, BigDecimal totalAmount) {
}
