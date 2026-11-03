package net.wowdev.ai.expensesagent.tools;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.wowdev.ai.expensesagent.model.Expense;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import net.wowdev.ai.expensesagent.service.ChatTrace;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class DeleteExpenseTool extends AbstractExpenseTool {
  private static final long CONFIRMATION_TTL_SECONDS = 900;
  private final ConcurrentMap<String, PendingDeletion> pendingDeletions =
      new ConcurrentHashMap<>();

  public DeleteExpenseTool(
      ExpenseRepository expenseRepository, ConfigurationRepository configurationRepository) {
    super(expenseRepository, configurationRepository);
  }

  @Tool(
      description =
          "Deletes an expense or income transaction by its ID, but only after asking the user for "
              + "explicit confirmation. First identify the record and call with confirmed=false. "
              + "Wait for the user's clear confirmation before calling again with the same ID and "
              + "confirmed=true. Never infer confirmation from the original delete request.")
  public String deleteExpense(ExpenseToolInputs.DeleteExpenseInput input, ToolContext toolContext) {
    if (input.id() == null) {
      return "A transaction ID is required to delete a record.";
    }

    String conversationId = conversationId(toolContext);
    Instant now = Instant.now();
    PendingDeletion pending = pendingDeletions.get(conversationId);
    if (pending != null && pending.createdAt().plusSeconds(CONFIRMATION_TTL_SECONDS).isBefore(now)) {
      pendingDeletions.remove(conversationId, pending);
      pending = null;
    }

    if (pending != null && pending.id().equals(input.id()) && input.confirmed()) {
      Expense expense = expenseRepository.findById(input.id()).orElse(null);
      pendingDeletions.remove(conversationId, pending);
      if (expense == null) {
        return "No transaction found with ID " + input.id() + ".";
      }
      expenseRepository.delete(expense);
      String result = "Deleted transaction ID " + input.id() + ".";
      ChatTrace.record("deleteExpense", input.toString());
      return result;
    }

    Expense expense = expenseRepository.findById(input.id()).orElse(null);
    if (expense == null) {
      pendingDeletions.remove(conversationId);
      return "No transaction found with ID " + input.id() + ".";
    }

    pendingDeletions.put(conversationId, new PendingDeletion(input.id(), now));
    String result =
        "Are you sure you want to delete transaction ID "
            + input.id()
            + " ("
            + expense.getDate()
            + ", "
            + expense.getCategory()
            + ", $"
            + String.format(java.util.Locale.US, "%.2f", expense.getAmount())
            + ")? Please confirm before I delete it.";
    ChatTrace.record("deleteExpense", input.toString());
    return result;
  }

  private static String conversationId(ToolContext toolContext) {
    if (toolContext == null
        || toolContext.getContext() == null
        || toolContext.getContext().get("conversationId") == null) {
      return "default";
    }
    return toolContext.getContext().get("conversationId").toString();
  }

  private record PendingDeletion(Long id, Instant createdAt) {}
}
