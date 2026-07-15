package com.prakash.clinicos.ai.dto.response;

import java.util.List;

/**
 * reply — the model's final natural-language answer.
 * toolCalls — a human-readable trace of every tool the model invoked to get
 * there (name + raw JSON arguments), useful for debugging and for showing
 * "what the assistant actually did" in a UI rather than a black box.
 */
public record AssistantReplyResponse(String reply, List<String> toolCalls) {
}
