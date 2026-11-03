package net.wowdev.ai.expensesagent.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import net.wowdev.ai.expensesagent.tools.AddExpenseTool;
import net.wowdev.ai.expensesagent.tools.GetExpensesByCategoryTool;
import net.wowdev.ai.expensesagent.tools.GetFinancialSummaryTool;
import net.wowdev.ai.expensesagent.tools.GetSpendingBreakdownTool;
import net.wowdev.ai.expensesagent.tools.GetTotalSpendingTool;
import net.wowdev.ai.expensesagent.tools.GetTransactionsTool;
import net.wowdev.ai.expensesagent.tools.SetDefaultMonthlyIncomeTool;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import java.util.function.Consumer;
import org.springframework.ai.chat.memory.ChatMemory;

class ChatServiceTest {
  @Test
  void forwardsPromptConversationAndToolsAndReturnsContent() {
    ChatClient client = mock(ChatClient.class);
    ChatClient.ChatClientRequestSpec request = mock(ChatClient.ChatClientRequestSpec.class);
    ChatClient.CallResponseSpec call = mock(ChatClient.CallResponseSpec.class);
    ChatResponse chatResponse = mock(ChatResponse.class);
    Generation generation = mock(Generation.class);
    AssistantMessage assistantMessage = mock(AssistantMessage.class);
    ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
    Usage usage = mock(Usage.class);
    ToolMocks tools = mockTools();
    when(client.prompt()).thenReturn(request);
    when(request.user("How much?" )).thenReturn(request);
    when(request.advisors(any(Consumer.class))).thenReturn(request);
    when(request.tools(any(Object[].class))).thenReturn(request);
    when(request.toolContext(anyMap())).thenReturn(request);
    when(request.call()).thenReturn(call);
    when(call.chatResponse()).thenReturn(chatResponse);
    when(chatResponse.getResult()).thenReturn(generation);
    when(generation.getOutput()).thenReturn(assistantMessage);
    when(assistantMessage.getText()).thenReturn("$12");
    when(chatResponse.getMetadata()).thenReturn(metadata);
    when(metadata.getUsage()).thenReturn(usage);
    when(usage.getPromptTokens()).thenReturn(20);
    when(usage.getCompletionTokens()).thenReturn(4);
    when(usage.getTotalTokens()).thenReturn(24);
    assertEquals(new ChatTurn("How much?", "No expense tools were called. The assistant used your prompt and conversation context to respond.", "$12", new TokenUsage(20, 4, 24)),
        new ChatService(client, tools.add(), tools.byCategory(), tools.total(), tools.summary(), tools.breakdown(), tools.salary(), tools.transactions())
            .askAgent("How much?", "u:s"));
    ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> advisorCaptor = ArgumentCaptor.forClass(Consumer.class);
    verify(request).advisors(advisorCaptor.capture());
    ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class);
    advisorCaptor.getValue().accept(advisorSpec);
    verify(advisorSpec).param(ChatMemory.CONVERSATION_ID, "u:s");
    ArgumentCaptor<Object[]> toolsCaptor = ArgumentCaptor.forClass(Object[].class);
    verify(request).tools(toolsCaptor.capture());
    assertArrayEquals(tools.asArray(), toolsCaptor.getValue());
    verify(request).toolContext(java.util.Map.of("conversationId", "u:s"));
  }

  @Test
  void handlesMissingModelResponseAndTokenUsage() {
    ChatClient client = mock(ChatClient.class);
    ChatClient.ChatClientRequestSpec request = mock(ChatClient.ChatClientRequestSpec.class);
    ChatClient.CallResponseSpec call = mock(ChatClient.CallResponseSpec.class);
    ToolMocks tools = mockTools();
    when(client.prompt()).thenReturn(request);
    when(request.user("hello")).thenReturn(request);
    when(request.advisors(any(Consumer.class))).thenReturn(request);
    when(request.tools(any(Object[].class))).thenReturn(request);
    when(request.toolContext(anyMap())).thenReturn(request);
    when(request.call()).thenReturn(call);
    when(call.chatResponse()).thenReturn(null);

    assertEquals(
        new ChatTurn(
            "hello",
            "No expense tools were called. The assistant used your prompt and conversation context to respond.",
            "",
            TokenUsage.unavailable()),
        new ChatService(client, tools.add(), tools.byCategory(), tools.total(), tools.summary(), tools.breakdown(), tools.salary(), tools.transactions())
            .askAgent("hello", "u:s"));
  }

  private static ToolMocks mockTools() {
    return new ToolMocks(
        mock(AddExpenseTool.class),
        mock(GetExpensesByCategoryTool.class),
        mock(GetTotalSpendingTool.class),
        mock(GetFinancialSummaryTool.class),
        mock(GetSpendingBreakdownTool.class),
        mock(SetDefaultMonthlyIncomeTool.class),
        mock(GetTransactionsTool.class));
  }

  private record ToolMocks(
      AddExpenseTool add,
      GetExpensesByCategoryTool byCategory,
      GetTotalSpendingTool total,
      GetFinancialSummaryTool summary,
      GetSpendingBreakdownTool breakdown,
      SetDefaultMonthlyIncomeTool salary,
      GetTransactionsTool transactions) {
    Object[] asArray() {
      return new Object[] {add, byCategory, total, summary, breakdown, salary, transactions};
    }
  }
}
