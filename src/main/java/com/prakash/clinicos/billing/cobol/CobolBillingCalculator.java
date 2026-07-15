package com.prakash.clinicos.billing.cobol;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Cross-language IPC bridge to the compiled GnuCOBOL billing engine
 * (src/main/cobol/BillingCalc.cbl).
 *
 * Why hand off discount/tax/total math to a COBOL subprocess instead of
 * doing it in Java? COBOL's DISPLAY numerics are exact fixed-point decimal —
 * there is no IEEE-754 binary float underneath to introduce representation
 * error on values like 0.10. This is the same reason banking cores still run
 * COBOL for money math. Java's BigDecimal is also exact, so the numeric
 * result is identical either way; this class exists to demonstrate — and
 * actually exercise — the pattern of delegating financial calculations to
 * a dedicated fixed-point engine over a process boundary.
 *
 * Wire protocol: one fixed-width ASCII line in on stdin, one fixed-width
 * line out on stdout. Every field is unsigned digits with an implied
 * 2-decimal scale (no separators) — see the layout comment in the .cbl file.
 */
@Service
@Slf4j
public class CobolBillingCalculator {

    private static final int SUBTOTAL_INT_DIGITS = 7;
    private static final int PCT_INT_DIGITS = 3;
    private static final int DISCOUNT_AMT_INT_DIGITS = 7;
    private static final int OUTPUT_FIELD_WIDTH = SUBTOTAL_INT_DIGITS + 2; // 9
    private static final int OUTPUT_LENGTH = OUTPUT_FIELD_WIDTH * 3; // discount + tax + total

    private final String binaryPath;

    public CobolBillingCalculator(
            @Value("${app.cobol.binary-path:target/cobol/billing_calc}") String binaryPath) {
        this.binaryPath = binaryPath;
    }

    /**
     * Runs one subtotal/discount/tax calculation through the COBOL engine.
     *
     * @throws CobolUnavailableException if the binary is missing, times out,
     *         or fails — callers should catch this and fall back to Java math.
     */
    public BillingCalcResult calculate(BigDecimal subtotal, BigDecimal discountPercent,
                                        BigDecimal discountAmount, BigDecimal taxPercent) {
        Path binary = Path.of(binaryPath);
        if (!Files.isExecutable(binary)) {
            throw new CobolUnavailableException(
                    "COBOL billing binary not found or not executable: " + binary.toAbsolutePath()
                            + " (run scripts/compile-cobol.sh)");
        }

        String input = encode(subtotal, SUBTOTAL_INT_DIGITS)
                + encode(discountPercent, PCT_INT_DIGITS)
                + encode(discountAmount, DISCOUNT_AMT_INT_DIGITS)
                + encode(taxPercent, PCT_INT_DIGITS);

        try {
            Process process = new ProcessBuilder(binary.toString())
                    .redirectErrorStream(true)
                    .start();

            try (var stdin = process.getOutputStream()) {
                stdin.write(input.getBytes(StandardCharsets.US_ASCII));
                stdin.write('\n');
            }

            String output;
            try (var stdout = process.getInputStream()) {
                output = new String(stdout.readAllBytes(), StandardCharsets.US_ASCII).trim();
            }

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new CobolUnavailableException("COBOL billing engine timed out after 5s");
            }
            if (process.exitValue() != 0) {
                throw new CobolUnavailableException(
                        "COBOL billing engine exited with code " + process.exitValue() + ": " + output);
            }
            if (output.length() != OUTPUT_LENGTH) {
                throw new CobolUnavailableException(
                        "Unexpected COBOL output length " + output.length() + ": \"" + output + "\"");
            }

            BigDecimal discount = decode(output.substring(0, OUTPUT_FIELD_WIDTH));
            BigDecimal tax = decode(output.substring(OUTPUT_FIELD_WIDTH, OUTPUT_FIELD_WIDTH * 2));
            BigDecimal total = decode(output.substring(OUTPUT_FIELD_WIDTH * 2, OUTPUT_FIELD_WIDTH * 3));
            return new BillingCalcResult(discount, tax, total);

        } catch (IOException e) {
            throw new CobolUnavailableException("Failed to invoke COBOL billing engine", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CobolUnavailableException("Interrupted while invoking COBOL billing engine", e);
        }
    }

    /** Encodes a non-negative BigDecimal as zero-padded digits, implied 2-decimal scale. */
    private static String encode(BigDecimal value, int intDigits) {
        if (value.signum() < 0) {
            throw new IllegalArgumentException("COBOL billing fields must be non-negative: " + value);
        }
        BigInteger unscaled = value.setScale(2, RoundingMode.HALF_UP).unscaledValue();
        int width = intDigits + 2;
        if (unscaled.toString().length() > width) {
            throw new IllegalArgumentException(
                    "Value " + value + " exceeds the COBOL engine's " + intDigits + "-digit field width");
        }
        return String.format("%0" + width + "d", unscaled);
    }

    /** Decodes a fixed-width digit segment back to a scale-2 BigDecimal. */
    private static BigDecimal decode(String segment) {
        return new BigDecimal(new BigInteger(segment), 2);
    }
}
