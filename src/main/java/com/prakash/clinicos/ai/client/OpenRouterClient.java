package com.prakash.clinicos.ai.client;

import com.prakash.clinicos.ai.dto.ChatCompletionRequest;
import com.prakash.clinicos.ai.dto.ChatCompletionResponse;
import com.prakash.clinicos.ai.dto.ChatMessage;
import com.prakash.clinicos.ai.dto.ToolDefinition;
import com.prakash.clinicos.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Thin wrapper around OpenRouter's chat-completions endpoint
 * (https://openrouter.ai/api/v1/chat/completions) — an OpenAI-compatible
 * API that fans requests out across many underlying model providers.
 *
 * Why OpenRouter instead of calling one model vendor directly? A single API
 * key and wire format works against dozens of models (including free-tier
 * ones), so the model can be swapped via config (app.ai.openrouter.model)
 * without touching any Java code.
 *
 * apiKey is intentionally allowed to be blank — this lets the rest of the
 * application boot and run normally on a machine that hasn't set
 * OPENROUTER_API_KEY yet. Calling chat() without a key throws a clear 503
 * rather than the app silently doing nothing or crashing at startup.
 */
@Component
@Slf4j
public class OpenRouterClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public OpenRouterClient(
            @Value("${app.ai.openrouter.base-url:https://openrouter.ai/api/v1}") String baseUrl,
            @Value("${app.ai.openrouter.api-key:}") String apiKey,
            @Value("${app.ai.openrouter.model:meta-llama/llama-3.3-70b-instruct:free}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                // OpenRouter uses these to attribute traffic on the dashboard — optional but recommended
                .defaultHeader("HTTP-Referer", "https://github.com/prakash/clinicos")
                .defaultHeader("X-Title", "ClinicOS Clinical Assistant")
                .build();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Sends one chat-completion turn (with or without tool definitions) and
     * returns the model's response message. Callers own the multi-turn loop —
     * this method is a single HTTP round trip.
     */
    public ChatMessage chat(List<ChatMessage> messages, List<ToolDefinition> tools) {
        if (!isConfigured()) {
            throw new AppException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI assistant is not configured — set the OPENROUTER_API_KEY environment variable");
        }

        ChatCompletionRequest request = new ChatCompletionRequest(model, messages, tools);

        ChatCompletionResponse response;
        try {
            response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(ChatCompletionResponse.class);
        } catch (RestClientException e) {
            log.error("OpenRouter request failed", e);
            throw new AppException(HttpStatus.BAD_GATEWAY, "AI assistant request failed: " + e.getMessage());
        }

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "AI assistant returned an empty response");
        }
        return response.getChoices().get(0).getMessage();
    }
}
