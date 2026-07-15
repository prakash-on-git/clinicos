package com.prakash.clinicos.ai.service;

import com.prakash.clinicos.ai.client.OpenRouterClient;
import com.prakash.clinicos.ai.dto.ChatMessage;
import com.prakash.clinicos.ai.dto.ToolCall;
import com.prakash.clinicos.ai.dto.response.AssistantReplyResponse;
import com.prakash.clinicos.ai.tool.ClinicalTool;
import com.prakash.clinicos.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the agentic tool-use loop — pure Mockito, no real OpenRouter
 * call. Verifies orchestration behavior that can't be exercised without a
 * live API key: multi-round tool execution, the max-rounds circuit breaker,
 * and unknown-tool handling.
 */
@ExtendWith(MockitoExtension.class)
class ClinicalAssistantServiceTest {

    @Mock private OpenRouterClient openRouterClient;
    @Mock private ClinicalTool searchTool;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ClinicalAssistantService newService() {
        when(searchTool.name()).thenReturn("search_patients");
        when(searchTool.description()).thenReturn("desc");
        when(searchTool.parametersSchema()).thenReturn(Map.of("type", "object"));
        return new ClinicalAssistantService(openRouterClient, List.of(searchTool), objectMapper);
    }

    @Test
    void returnsFinalAnswerImmediatelyWhenModelMakesNoToolCalls() {
        ClinicalAssistantService service = newService();
        UserPrincipal principal = mock(UserPrincipal.class);

        ChatMessage finalAnswer = new ChatMessage("assistant", "The clinic is open until 6pm.");
        when(openRouterClient.chat(anyList(), anyList())).thenReturn(finalAnswer);

        AssistantReplyResponse result = service.chat(1L, principal, "What time do you close?");

        assertThat(result.reply()).isEqualTo("The clinic is open until 6pm.");
        assertThat(result.toolCalls()).isEmpty();
        verify(openRouterClient, times(1)).chat(anyList(), anyList());
    }

    @Test
    void executesToolCallAndFeedsResultBackForASecondRound() {
        ClinicalAssistantService service = newService();
        UserPrincipal principal = mock(UserPrincipal.class);

        ChatMessage toolCallMessage = new ChatMessage("assistant", null);
        ToolCall call = new ToolCall();
        call.setId("call_1");
        ToolCall.FunctionCall function = new ToolCall.FunctionCall();
        function.setName("search_patients");
        function.setArguments("{\"query\":\"john\"}");
        call.setFunction(function);
        toolCallMessage.setToolCalls(List.of(call));

        ChatMessage finalAnswer = new ChatMessage("assistant", "Found John Doe, patientId 42.");

        when(openRouterClient.chat(anyList(), anyList()))
                .thenReturn(toolCallMessage)
                .thenReturn(finalAnswer);
        when(searchTool.execute(eq(1L), eq(principal), eq("{\"query\":\"john\"}")))
                .thenReturn(Map.of("patientId", 42, "fullName", "John Doe"));

        AssistantReplyResponse result = service.chat(1L, principal, "Find patient John");

        assertThat(result.reply()).isEqualTo("Found John Doe, patientId 42.");
        assertThat(result.toolCalls()).hasSize(1);
        assertThat(result.toolCalls().get(0)).contains("search_patients");
        verify(openRouterClient, times(2)).chat(anyList(), anyList());
        verify(searchTool).execute(1L, principal, "{\"query\":\"john\"}");
    }

    @Test
    void stopsAfterMaxToolRoundsInsteadOfLoopingForever() {
        ClinicalAssistantService service = newService();
        UserPrincipal principal = mock(UserPrincipal.class);

        ChatMessage toolCallMessage = new ChatMessage("assistant", null);
        ToolCall call = new ToolCall();
        call.setId("call_x");
        ToolCall.FunctionCall function = new ToolCall.FunctionCall();
        function.setName("search_patients");
        function.setArguments("{\"query\":\"x\"}");
        call.setFunction(function);
        toolCallMessage.setToolCalls(List.of(call));

        // Model calls a tool every single round, never settling on a final answer
        when(openRouterClient.chat(anyList(), anyList())).thenReturn(toolCallMessage);
        when(searchTool.execute(eq(1L), eq(principal), eq("{\"query\":\"x\"}")))
                .thenReturn(Map.of("result", "nothing useful"));

        AssistantReplyResponse result = service.chat(1L, principal, "Keep searching");

        assertThat(result.reply()).contains("wasn't able to finish");
        assertThat(result.toolCalls()).hasSize(4); // MAX_TOOL_ROUNDS
        verify(openRouterClient, times(4)).chat(anyList(), anyList());
    }

    @Test
    void toolFailureIsReportedBackToTheModelInsteadOfThrowing() {
        ClinicalAssistantService service = newService();
        UserPrincipal principal = mock(UserPrincipal.class);

        ChatMessage toolCallMessage = new ChatMessage("assistant", null);
        ToolCall call = new ToolCall();
        call.setId("call_err");
        ToolCall.FunctionCall function = new ToolCall.FunctionCall();
        function.setName("search_patients");
        function.setArguments("{\"query\":\"boom\"}");
        call.setFunction(function);
        toolCallMessage.setToolCalls(List.of(call));

        ChatMessage finalAnswer = new ChatMessage("assistant", "I couldn't complete that search.");

        when(openRouterClient.chat(anyList(), anyList()))
                .thenReturn(toolCallMessage)
                .thenReturn(finalAnswer);
        when(searchTool.execute(eq(1L), eq(principal), eq("{\"query\":\"boom\"}")))
                .thenThrow(new RuntimeException("clinic not found"));

        AssistantReplyResponse result = service.chat(1L, principal, "Find patient boom");

        assertThat(result.reply()).isEqualTo("I couldn't complete that search.");
        verify(openRouterClient, times(2)).chat(anyList(), anyList());
    }
}
