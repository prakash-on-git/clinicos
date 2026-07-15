package com.prakash.clinicos.ai.controller;

import com.prakash.clinicos.ai.dto.request.AssistantPromptRequest;
import com.prakash.clinicos.ai.dto.response.AssistantReplyResponse;
import com.prakash.clinicos.ai.service.ClinicalAssistantService;
import com.prakash.clinicos.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Natural-language clinical assistant — search patients, check availability,
 * look up history, book appointments, and draft prescriptions from a single
 * prompt, via an agentic tool-use loop over OpenRouter.
 *
 * Front-desk staff only, same as the endpoints its tools wrap (queue/booking).
 * Patient data flows through the same clinic-scoped services every other
 * controller uses, so the assistant is bound by the same tenant isolation.
 */
@Tag(name = "AI Assistant", description = "Agentic clinical assistant — natural-language booking, patient history, and prescription drafts.")
@RestController
@RequestMapping("/api/v1/clinics/{clinicId}/ai")
public class ClinicalAssistantController {

    private final ClinicalAssistantService assistantService;

    public ClinicalAssistantController(ClinicalAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    /**
     * POST /api/v1/clinics/{clinicId}/ai/assistant
     *
     * Returns 503 if OPENROUTER_API_KEY isn't set — the assistant degrades to
     * "unavailable" rather than the app failing to start without it.
     */
    @PostMapping("/assistant")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AssistantReplyResponse> chat(
            @PathVariable Long clinicId,
            @Valid @RequestBody AssistantPromptRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(assistantService.chat(clinicId, principal, request.getPrompt()));
    }
}
