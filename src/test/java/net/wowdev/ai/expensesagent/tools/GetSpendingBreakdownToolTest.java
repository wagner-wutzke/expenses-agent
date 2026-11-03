package net.wowdev.ai.expensesagent.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;

class GetSpendingBreakdownToolTest {
  ExpenseRepository expenses = mock(ExpenseRepository.class);
  GetSpendingBreakdownTool tool = new GetSpendingBreakdownTool(expenses, mock(ConfigurationRepository.class));

  private String currentMonthLabel() {
    return YearMonth.now().format(DateTimeFormatter.ofPattern("MMMM uuuu", Locale.US));
  }

  @Test
  void handlesEmptyBreakdown() {
    when(expenses.getSpendingPerCategory(any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());
    assertEquals("No spending data available for " + currentMonthLabel() + " to construct a breakdown chart.",
        tool.getSpendingBreakdown());
  }

  @Test
  void formatsCategoryTotalsAsMarkdown() {
    when(expenses.getSpendingPerCategory(any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of(new Object[] {"Food", 12.5}, new Object[] {"Rent", 30.0}));
    assertEquals(
        "Spending Category Allocation Chart Data (" + currentMonthLabel() + "):\n| Category | Total Spent ($) |\n|---|---|\n| Food | 12.50 |\n| Rent | 30.00 |\n",
        tool.getSpendingBreakdown());
  }
}
