package com.prakash.clinicos.ai.service;

import com.prakash.clinicos.ai.client.OpenRouterClient;
import com.prakash.clinicos.ai.dto.ChatMessage;
import com.prakash.clinicos.ai.dto.ToolCall;
import com.prakash.clinicos.ai.dto.ToolDefinition;
import com.prakash.clinicos.ai.dto.response.AssistantReplyResponse;
import com.prakash.clinicos.ai.tool.ClinicalTool;
import com.prakash.clinicos.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Runs the agentic tool-use loop: send the conversation + available tools to
 * the model, execute whatever tools it calls against the real backend, feed
 * the results back, and repeat until the model answers in plain text.
 *
 * Why a bounded loop instead of trusting the model to stop? A model can get
 * stuck calling the same tool repeatedly (bad arguments, misreading a
 * result). MAX_TOOL_ROUNDS caps the damage — and the cost — of that failure
 * mode instead of looping (and billing OpenRouter) forever.
 */
@Service
@Slf4j
public class ClinicalAssistantService {

    private static final int MAX_TOOL_ROUNDS = 4;

    private static final String SYSTEM_PROMPT = """
            You are the clinical front-desk assistant for a single clinic in the ClinicOS platform.
            You can search patients, check doctor availability, look up a patient's appointment
            history, and book appointments using the tools provided.

            Rules:
            - Always resolve a patient's name to a patientId via search_patients before booking or
              looking up history, unless the caller already gave you a numeric patientId.
            - Always call check_doctor_availability before book_appointment, and only use a
              startTime that appears in that tool's freeSlots.
            - If asked to draft a prescription, write it directly in your final answer as
              medicines, diagnosis, and instructions — do not invent a tool for this. Prescription
              drafts are for a doctor to review and enter themselves; you never persist one.
            - If a request is ambiguous (e.g. multiple patients match a name), ask a clarifying
              question instead of guessing.
            - Keep replies concise and clinical in tone.
            """;

    private final OpenRouterClient openRouterClient;
    private final List<ClinicalTool> tools;
    private final ObjectMapper objectMapper;

    public ClinicalAssistantService(OpenRouterClient openRouterClient, List<ClinicalTool> tools,
                                     ObjectMapper objectMapper) {
        this.openRouterClient = openRouterClient;
        this.tools = tools;
        this.objectMapper = objectMapper;
    }

    public AssistantReplyResponse chat(Long clinicId, UserPrincipal principal, String prompt) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(SYSTEM_PROMPT));
        messages.add(ChatMessage.user(prompt));

        List<ToolDefinition> toolDefinitions = tools.stream().map(this::toDefinition).toList();
        List<String> toolCallLog = new ArrayList<>();

        for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
            ChatMessage assistantMessage = openRouterClient.chat(messages, toolDefinitions);
            messages.add(assistantMessage);

            List<ToolCall> toolCalls = assistantMessage.getToolCalls();
            if (toolCalls == null || toolCalls.isEmpty()) {
                return new AssistantReplyResponse(assistantMessage.getContent(), toolCallLog);
            }

            for (ToolCall call : toolCalls) {
                String toolName = call.getFunction().getName();
                String arguments = call.getFunction().getArguments();
                toolCallLog.add(toolName + "(" + arguments + ")");

                String resultJson = runTool(clinicId, principal, toolName, arguments);
                messages.add(ChatMessage.toolResult(call.getId(), resultJson));
            }
        }

        log.warn("Clinical assistant hit MAX_TOOL_ROUNDS ({}) without a final answer, clinicId={}",
                MAX_TOOL_ROUNDS, clinicId);
        return new AssistantReplyResponse(
                "I wasn't able to finish that request in a reasonable number of steps — "
                        + "please try rephrasing or breaking it into smaller requests.",
                toolCallLog);
    }

    private String runTool(Long clinicId, UserPrincipal principal, String toolName, String argumentsJson) {
        ClinicalTool tool = tools.stream()
                .filter(t -> t.name().equals(toolName))
                .findFirst()
                .orElse(null);

        if (tool == null) {
            return toJson(Map.of("error", "Unknown tool: " + toolName));
        }

        try {
            Object result = tool.execute(clinicId, principal, argumentsJson);
            return toJson(result);
        } catch (Exception e) {
            // Errors go back to the model as a tool result (not thrown), so it can
            // recover — e.g. re-search with different arguments — instead of the
            // whole conversation failing on one bad tool call.
            log.warn("Tool {} failed: {}", toolName, e.getMessage());
            return toJson(Map.of("error", e.getMessage() != null ? e.getMessage() : "Tool execution failed"));
        }
    }

    private String toJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    private ToolDefinition toDefinition(ClinicalTool tool) {
        return new ToolDefinition(new ToolDefinition.FunctionSpec(
                tool.name(), tool.description(), tool.parametersSchema()));
    }
}
