package net.wowdev.ai.expensesagent.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import net.wowdev.ai.expensesagent.model.Expense;
import net.wowdev.ai.expensesagent.model.ExpenseType;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;

class UpdateExpenseToolTest {
  private final ExpenseRepository expenses = mock(ExpenseRepository.class);
  private final UpdateExpenseTool tool =
      new UpdateExpenseTool(expenses, mock(ConfigurationRepository.class));

  @Test
  void updatesOnlyTheFieldsProvidedAndPreservesTheCompleteDescription() {
    Expense existing =
        new Expense(7L, LocalDate.of(2026, 10, 2), "Food", "Original note", 12.0);
    when(expenses.findById(7L)).thenReturn(Optional.of(existing));
    when(expenses.save(existing)).thenReturn(existing);
    String fullDescription = "Dinner with Sam; shared soup, bread, dessert, and tea after the show.";

    assertEquals(
        "Updated transaction ID 7.",
        tool.updateExpense(
            new ExpenseToolInputs.UpdateExpenseInput(
                7L, 18.5, null, fullDescription, "2026-10-03", ExpenseType.CREDIT)));

    assertEquals(18.5, existing.getAmount());
    assertEquals("Food", existing.getCategory());
    assertEquals(fullDescription, existing.getDescription());
    assertEquals(LocalDate.of(2026, 10, 3), existing.getDate());
    assertEquals(ExpenseType.CREDIT, existing.getType());
    verify(expenses).save(existing);
  }

  @Test
  void rejectsMissingIdAndEmptyUpdates() {
    assertEquals(
        "A transaction ID is required to update a record.",
        tool.updateExpense(new ExpenseToolInputs.UpdateExpenseInput(null, 1.0, null, null, null, null)));
    assertEquals(
        "No changes were provided for transaction ID 4.",
        tool.updateExpense(new ExpenseToolInputs.UpdateExpenseInput(4L, null, null, null, null, null)));
    verifyNoInteractions(expenses);
  }

  @Test
  void reportsUnknownRecordWithoutSaving() {
    when(expenses.findById(99L)).thenReturn(Optional.empty());

    assertEquals(
        "No transaction found with ID 99.",
        tool.updateExpense(
            new ExpenseToolInputs.UpdateExpenseInput(99L, null, "Transport", null, null, null)));
    verify(expenses, never()).save(any());
  }
}
