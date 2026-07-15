package com.prakash.clinicos.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * One message in an OpenRouter/OpenAI-compatible chat completion exchange.
 *
 * Getter+Setter (not @Builder) on purpose — this round-trips through Jackson
 * in both directions (request out, response in via RestClient), and a
 * builder-only class has no default constructor for Jackson to deserialize
 * into (see RedisConfig's comment for the bug this exact pattern caused
 * with the availability cache).
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage {

    /** "system" | "user" | "assistant" | "tool" */
    private String role;

    private String content;

    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;

    /** Set only on role="tool" messages — links the result back to the call that produced it. */
    @JsonProperty("tool_call_id")
    private String toolCallId;

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }

    public static ChatMessage toolResult(String toolCallId, String content) {
        ChatMessage message = new ChatMessage("tool", content);
        message.setToolCallId(toolCallId);
        return message;
    }
}
