package com.prakash.clinicos.ai.tool;

import com.prakash.clinicos.security.UserPrincipal;

import java.util.Map;

/**
 * One action the clinical assistant can invoke against the real backend.
 *
 * Implementations never take clinicId from the model's arguments — the
 * orchestrating service (ClinicalAssistantService) injects it from the
 * authenticated request path, the same tenant-scoping rule every other
 * controller in this app follows. A tool that let the model supply its own
 * clinicId would be a cross-tenant data leak waiting to happen.
 */
public interface ClinicalTool {

    /** Must match the name advertised in the tool's JSON-schema spec exactly. */
    String name();

    String description();

    /** JSON Schema (type=object) describing the arguments the model must supply. */
    Map<String, Object> parametersSchema();

    /**
     * Executes the tool. argumentsJson is the raw JSON string the model produced
     * for this call — implementations parse only the fields they declared in
     * parametersSchema().
     */
    Object execute(Long clinicId, UserPrincipal principal, String argumentsJson);
}
