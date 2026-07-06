package com.prakash.clinicos.common.util;

/**
 * Utility for generating URL-safe slug strings.
 * Example: "Sunrise Clinic & Diagnostics" → "sunrise-clinic-diagnostics"
 */
public final class SlugUtils {

    private SlugUtils() {}

    public static String slugify(String input) {
        if (input == null || input.isBlank()) return "";
        return input
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")  // remove special chars
                .replaceAll("[\\s-]+", "-")          // collapse spaces/hyphens into single hyphen
                .replaceAll("^-|-$", "");            // strip leading/trailing hyphens
    }
}
