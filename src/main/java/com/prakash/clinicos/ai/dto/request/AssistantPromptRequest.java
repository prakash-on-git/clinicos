package com.prakash.clinicos.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AssistantPromptRequest {

    @NotBlank(message = "prompt is required")
    private String prompt;
}
