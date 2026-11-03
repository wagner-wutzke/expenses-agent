package net.wowdev.ai.expensesagent.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import net.wowdev.ai.expensesagent.model.Expense;
import net.wowdev.ai.expensesagent.model.ExpenseType;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;

class GetExpensesByCategoryToolTest {
  ExpenseRepository expenses = mock(ExpenseRepository.class);
  GetExpensesByCategoryTool tool = new GetExpensesByCategoryTool(expenses, mock(ConfigurationRepository.class));

  @Test
  void returnsNoResultsForEmptyCategory() {
    when(expenses.findByCategoryIgnoreCaseAndDateGreaterThanEqualAndDateLessThan(
            eq("Books"), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());
    assertEquals("No expenses found for category Books in this month.",
        tool.getExpensesByCategoryTool(new ExpenseToolInputs.CategoryInput("Books")));
  }

  @Test
  void listsDebitsAndCreditsForCategory() {
    when(expenses.findByCategoryIgnoreCaseAndDateGreaterThanEqualAndDateLessThan(
            eq("Food"), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of(
            new Expense(1L, LocalDate.of(2026, 11, 1), "Food", "Lunch", 4.25),
            new Expense(2L, LocalDate.of(2026, 11, 2), "Food", "Refund", 2.0, ExpenseType.CREDIT)));
    assertEquals(
        "Transactions for Food (this month):\n- ID 1 | 2026-11-01 | Lunch | -$4.25 (debit)\n- ID 2 | 2026-11-02 | Refund | +$2.00 (credit)",
        tool.getExpensesByCategoryTool(new ExpenseToolInputs.CategoryInput("Food")));
  }
}
