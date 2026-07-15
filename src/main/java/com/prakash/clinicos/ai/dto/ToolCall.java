package com.prakash.clinicos.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A tool invocation requested by the model, per the OpenAI function-calling schema. */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolCall {

    private String id;

    /** Always "function" per the OpenAI/OpenRouter schema. */
    private String type = "function";

    private FunctionCall function;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FunctionCall {
        private String name;

        /** Raw JSON string of arguments — parsed per-tool, since each tool's shape differs. */
        private String arguments;
    }
}
