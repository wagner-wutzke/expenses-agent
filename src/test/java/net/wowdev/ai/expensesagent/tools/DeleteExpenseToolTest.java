package net.wowdev.ai.expensesagent.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import net.wowdev.ai.expensesagent.model.Expense;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;

class DeleteExpenseToolTest {
  private final ExpenseRepository expenses = mock(ExpenseRepository.class);
  private final DeleteExpenseTool tool =
      new DeleteExpenseTool(expenses, mock(ConfigurationRepository.class));

  @Test
  void asksForConfirmationThenDeletesSameRecordAfterConfirmation() {
    Expense expense = new Expense(15L, LocalDate.of(2026, 10, 12), "Dining", "Lunch", 23.5);
    when(expenses.findById(15L)).thenReturn(Optional.of(expense));
    ToolContext context = new ToolContext(Map.of("conversationId", "delete-confirm"));

    String confirmation = tool.deleteExpense(new ExpenseToolInputs.DeleteExpenseInput(15L, false), context);
    assertTrue(confirmation.contains("transaction ID 15"));
    assertTrue(confirmation.contains("2026-10-12, Dining, $23.50"));
    verify(expenses, never()).delete(any(Expense.class));

    assertEquals(
        "Deleted transaction ID 15.",
        tool.deleteExpense(new ExpenseToolInputs.DeleteExpenseInput(15L, true), context));
    verify(expenses).delete(expense);
  }

  @Test
  void confirmationWithoutPriorRequestDoesNotDelete() {
    Expense expense = new Expense(20L, LocalDate.now(), "Transport", "Taxi", 12.0);
    when(expenses.findById(20L)).thenReturn(Optional.of(expense));

    assertTrue(
        tool.deleteExpense(new ExpenseToolInputs.DeleteExpenseInput(20L, true), null)
            .contains("Please confirm"));
    verify(expenses, never()).delete(any(Expense.class));
  }

  @Test
  void doesNotAcceptConfirmationForAnotherConversationOrDifferentId() {
    Expense first = new Expense(1L, LocalDate.now(), "Dining", "Soup", 8.0);
    Expense second = new Expense(2L, LocalDate.now(), "Dining", "Tea", 3.0);
    when(expenses.findById(1L)).thenReturn(Optional.of(first));
    when(expenses.findById(2L)).thenReturn(Optional.of(second));
    ToolContext firstConversation = new ToolContext(Map.of("conversationId", "first"));
    ToolContext otherConversation = new ToolContext(Map.of("conversationId", "second"));

    tool.deleteExpense(new ExpenseToolInputs.DeleteExpenseInput(1L, false), firstConversation);
    assertTrue(
        tool.deleteExpense(new ExpenseToolInputs.DeleteExpenseInput(1L, true), otherConversation)
            .contains("Please confirm"));
    assertTrue(
        tool.deleteExpense(new ExpenseToolInputs.DeleteExpenseInput(2L, true), firstConversation)
            .contains("Please confirm"));
    verify(expenses, never()).delete(any(Expense.class));
  }

  @Test
  void handlesMissingIdAndUnknownRecord() {
    assertEquals(
        "A transaction ID is required to delete a record.",
        tool.deleteExpense(new ExpenseToolInputs.DeleteExpenseInput(null, false), null));
    when(expenses.findById(404L)).thenReturn(Optional.empty());
    assertEquals(
        "No transaction found with ID 404.",
        tool.deleteExpense(new ExpenseToolInputs.DeleteExpenseInput(404L, false), null));
  }

  @Test
  void doesNotDeleteIfTheRecordDisappearsBeforeConfirmation() {
    Expense expense = new Expense(30L, LocalDate.now(), "Housing", "Repair", 50.0);
    when(expenses.findById(30L)).thenReturn(Optional.of(expense), Optional.empty());
    ToolContext context = new ToolContext(Map.of("conversationId", "vanished"));

    tool.deleteExpense(new ExpenseToolInputs.DeleteExpenseInput(30L, false), context);
    assertEquals(
        "No transaction found with ID 30.",
        tool.deleteExpense(new ExpenseToolInputs.DeleteExpenseInput(30L, true), context));
    verify(expenses, never()).delete(any(Expense.class));
  }
}
