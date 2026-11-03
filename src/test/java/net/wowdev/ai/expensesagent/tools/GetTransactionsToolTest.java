package net.wowdev.ai.expensesagent.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import net.wowdev.ai.expensesagent.model.Expense;
import net.wowdev.ai.expensesagent.model.ExpenseType;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;

class GetTransactionsToolTest {
  private final ExpenseRepository expenses = mock(ExpenseRepository.class);
  private final GetTransactionsTool tool =
      new GetTransactionsTool(expenses, mock(ConfigurationRepository.class));

  @Test
  void listsTransactionsMatchingTypeAndCaseInsensitiveCategory() {
    YearMonth month = YearMonth.now();
    when(expenses.findByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(
            month.atDay(1), month.plusMonths(1).atDay(1)))
        .thenReturn(
            List.of(
                new Expense(1L, month.atDay(2), "Groceries", "Market vegetables", 24.5),
                new Expense(
                    2L,
                    month.atDay(3),
                    "Groceries",
                    "Store refund",
                    5.0,
                    ExpenseType.CREDIT),
                new Expense(3L, month.atDay(4), "Dining", "Lunch", 12.0)));

    assertEquals(
        "Expenses for this month:\n- "
            + month.atDay(2)
            + " | Groceries | Market vegetables: -$24.50 (debit)",
        tool.getTransactions(
            new ExpenseToolInputs.TransactionsInput(
                ExpenseType.DEBIT, "groc", 0)));
  }

  @Test
  void listsIncomeForRequestedPreviousMonth() {
    YearMonth previousMonth = YearMonth.now().minusMonths(1);
    when(expenses.findByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(
            previousMonth.atDay(1), previousMonth.plusMonths(1).atDay(1)))
        .thenReturn(
            List.of(
                new Expense(
                    1L,
                    previousMonth.atDay(7),
                    "Side work",
                    "Evening translation",
                    85.0,
                    ExpenseType.CREDIT)));

    assertEquals(
        "Income for last month:\n- "
            + previousMonth.atDay(7)
            + " | Side work | Evening translation: +$85.00 (credit)",
        tool.getTransactions(
            new ExpenseToolInputs.TransactionsInput(ExpenseType.CREDIT, null, 1)));
  }

  @Test
  void listsAllTypesWhenFiltersAreMissing() {
    YearMonth month = YearMonth.now();
    when(expenses.findByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(
            any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(
            List.of(
                new Expense(1L, month.atDay(1), "Dining", "Breakfast", 8.0),
                new Expense(
                    2L, month.atDay(2), "Sale", "Sold desk lamp", 16.0, ExpenseType.CREDIT)));

    assertEquals(
        "Transactions for this month:\n"
            + "- " + month.atDay(1) + " | Dining | Breakfast: -$8.00 (debit)\n"
            + "- " + month.atDay(2) + " | Sale | Sold desk lamp: +$16.00 (credit)",
        tool.getTransactions(new ExpenseToolInputs.TransactionsInput(null, " ", null)));
  }

  @Test
  void returnsNoMatchMessageWhenTypeAndCategoryExcludeAllRows() {
    YearMonth month = YearMonth.now();
    when(expenses.findByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(
            any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of(new Expense(1L, month.atDay(1), "Dining", "Lunch", 12.0)));

    assertEquals(
        "No matching transactions found for this month.",
        tool.getTransactions(
            new ExpenseToolInputs.TransactionsInput(ExpenseType.CREDIT, "Dining", 0)));
  }
}
