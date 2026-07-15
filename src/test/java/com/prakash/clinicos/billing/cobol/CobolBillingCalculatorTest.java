package com.prakash.clinicos.billing.cobol;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the real compiled binary when available (scripts/compile-cobol.sh
 * has been run), and always verifies the missing-binary failure path — the
 * one every machine without GnuCOBOL installed actually hits, and the one
 * BillingService relies on to trigger its Java fallback.
 */
class CobolBillingCalculatorTest {

    private static final String DEFAULT_BINARY = "target/cobol/billing_calc";

    @Test
    void calculatesDiscountTaxAndTotalUsingTheCompiledBinary() {
        Assumptions.assumeTrue(Files.isExecutable(Path.of(DEFAULT_BINARY)),
                "COBOL binary not compiled — run scripts/compile-cobol.sh first");

        CobolBillingCalculator calculator = new CobolBillingCalculator(DEFAULT_BINARY);

        BillingCalcResult result = calculator.calculate(
                new BigDecimal("150.05"), new BigDecimal("5.00"), BigDecimal.ZERO, new BigDecimal("5.00"));

        assertThat(result.discountAmount()).isEqualByComparingTo("7.50");
        assertThat(result.taxAmount()).isEqualByComparingTo("7.13");
        assertThat(result.totalAmount()).isEqualByComparingTo("149.68");
    }

    @Test
    void calculatesWithNoDiscountAndNoTax() {
        Assumptions.assumeTrue(Files.isExecutable(Path.of(DEFAULT_BINARY)),
                "COBOL binary not compiled — run scripts/compile-cobol.sh first");

        CobolBillingCalculator calculator = new CobolBillingCalculator(DEFAULT_BINARY);

        BillingCalcResult result = calculator.calculate(
                new BigDecimal("200.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        assertThat(result.discountAmount()).isEqualByComparingTo("0.00");
        assertThat(result.taxAmount()).isEqualByComparingTo("0.00");
        assertThat(result.totalAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    void throwsCobolUnavailableWhenBinaryIsMissing() {
        CobolBillingCalculator calculator = new CobolBillingCalculator("no/such/binary");

        assertThatThrownBy(() -> calculator.calculate(
                BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))
                .isInstanceOf(CobolUnavailableException.class)
                .hasMessageContaining("not found");
    }
}
