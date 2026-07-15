package com.prakash.clinicos.ai.tool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Small builders for the JSON-schema fragments tool parameter specs need. */
final class JsonSchema {

    private JsonSchema() {}

    static Map<String, Object> object(Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        return schema;
    }

    static Map<String, Object> string(String description) {
        return Map.of("type", "string", "description", description);
    }

    static Map<String, Object> integer(String description) {
        return Map.of("type", "integer", "description", description);
    }

    static Map<String, Object> properties(Object... nameSchemaPairs) {
        Map<String, Object> props = new LinkedHashMap<>();
        for (int i = 0; i < nameSchemaPairs.length; i += 2) {
            props.put((String) nameSchemaPairs[i], nameSchemaPairs[i + 1]);
        }
        return props;
    }
}
