package com.prakash.clinicos.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Response body from POST /chat/completions. Only the fields the assistant loop needs. */
@Getter
@Setter
@NoArgsConstructor
public class ChatCompletionResponse {

    private String id;
    private String model;
    private List<Choice> choices;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Choice {
        private int index;
        private ChatMessage message;

        @JsonProperty("finish_reason")
        private String finishReason;
    }
}
