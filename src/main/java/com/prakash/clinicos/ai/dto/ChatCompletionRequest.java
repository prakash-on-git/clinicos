package com.prakash.clinicos.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Request body for POST /chat/completions (OpenAI-compatible — OpenRouter proxies this shape). */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionRequest {

    private String model;
    private List<ChatMessage> messages;
    private List<ToolDefinition> tools;

    @JsonProperty("tool_choice")
    private String toolChoice;

    private Double temperature;

    public ChatCompletionRequest(String model, List<ChatMessage> messages, List<ToolDefinition> tools) {
        this.model = model;
        this.messages = messages;
        this.tools = (tools == null || tools.isEmpty()) ? null : tools;
        this.toolChoice = this.tools != null ? "auto" : null;
        this.temperature = 0.2; // low temperature — this assistant executes real bookings, not creative writing
    }
}
