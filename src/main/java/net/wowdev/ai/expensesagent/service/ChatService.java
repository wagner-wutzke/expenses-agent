package net.wowdev.ai.expensesagent.service;

import java.util.Map;
import net.wowdev.ai.expensesagent.tools.AddExpenseTool;
import net.wowdev.ai.expensesagent.tools.DeleteExpenseTool;
import net.wowdev.ai.expensesagent.tools.GetExpensesByCategoryTool;
import net.wowdev.ai.expensesagent.tools.GetFinancialSummaryTool;
import net.wowdev.ai.expensesagent.tools.GetSpendingBreakdownTool;
import net.wowdev.ai.expensesagent.tools.GetTotalSpendingTool;
import net.wowdev.ai.expensesagent.tools.GetTransactionsTool;
import net.wowdev.ai.expensesagent.tools.SetDefaultMonthlyIncomeTool;
import net.wowdev.ai.expensesagent.tools.UpdateExpenseTool;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

  private final ChatClient chatClient;

  private final Object[] tools;

  public ChatService(
      ChatClient chatClient,
      AddExpenseTool addExpenseTool,
      GetExpensesByCategoryTool getExpensesByCategoryTool,
      GetTotalSpendingTool getTotalSpendingTool,
      GetFinancialSummaryTool getFinancialSummaryTool,
      GetSpendingBreakdownTool getSpendingBreakdownTool,
      SetDefaultMonthlyIncomeTool setDefaultMonthlyIncomeTool,
      GetTransactionsTool getTransactionsTool,
      UpdateExpenseTool updateExpenseTool,
      DeleteExpenseTool deleteExpenseTool) {
    this.chatClient = chatClient;
    this.tools =
        new Object[] {
          addExpenseTool,
          getExpensesByCategoryTool,
          getTotalSpendingTool,
          getFinancialSummaryTool,
          getSpendingBreakdownTool,
          setDefaultMonthlyIncomeTool,
          getTransactionsTool,
          updateExpenseTool,
          deleteExpenseTool
        };
  }

  public ChatTurn askAgent(String userPrompt, String conversationId) {
    try (ChatTrace trace = ChatTrace.start()) {
      ChatResponse chatResponse =
          this.chatClient
              .prompt()
              .user(userPrompt)
              .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
              .tools(tools)
              .toolContext(Map.of("conversationId", conversationId))
              .call()
              .chatResponse();
      String response =
          chatResponse == null || chatResponse.getResult() == null
              ? ""
              : chatResponse.getResult().getOutput().getText();
      Usage usage =
          chatResponse == null || chatResponse.getMetadata() == null
              ? null
              : chatResponse.getMetadata().getUsage();
      TokenUsage tokenUsage =
          usage == null
              ? TokenUsage.unavailable()
              : new TokenUsage(
                  usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
      return new ChatTurn(
          userPrompt,
          trace.reasoningSummary(),
          response,
          tokenUsage,
          trace.transactionDetails());
    }
  }
}
