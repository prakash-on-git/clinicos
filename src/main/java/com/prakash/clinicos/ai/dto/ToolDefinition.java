package com.prakash.clinicos.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/** Advertises one callable tool to the model, per the OpenAI function-calling schema. */
@Getter
@Setter
@NoArgsConstructor
public class ToolDefinition {

    private String type = "function";
    private FunctionSpec function;

    public ToolDefinition(FunctionSpec function) {
        this.function = function;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FunctionSpec {
        private String name;
        private String description;

        /** JSON Schema object describing the tool's arguments. */
        private Map<String, Object> parameters;

        public FunctionSpec(String name, String description, Map<String, Object> parameters) {
            this.name = name;
            this.description = description;
            this.parameters = parameters;
        }
    }
}
