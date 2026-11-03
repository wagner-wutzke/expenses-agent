package net.wowdev.ai.expensesagent.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import net.wowdev.ai.expensesagent.model.ConfigurationEntry;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;

class GetFinancialSummaryToolTest {
  ExpenseRepository expenses = mock(ExpenseRepository.class);
  ConfigurationRepository configurations = mock(ConfigurationRepository.class);
  GetFinancialSummaryTool tool = new GetFinancialSummaryTool(expenses, configurations);

  @Test
  void includesDefaultSalaryInAvailableMonthlyTotal() {
    when(expenses.getTotalSpending(any(LocalDate.class), any(LocalDate.class))).thenReturn(750.0);
    when(expenses.getTotalCredits(any(LocalDate.class), any(LocalDate.class))).thenReturn(1200.0);
    when(configurations.findById("deafult.salary"))
        .thenReturn(Optional.of(new ConfigurationEntry("deafult.salary", "Salary", 5000.0)));
    assertEquals(
        "Total Spent this month: $750.00\nTotal Credit this month: $1200.00\nMonthly income: $5000.00\nTotal available this month: $5450.00",
        tool.getFinancialSummaryTool(new ExpenseToolInputs.EmptyInput()));
  }

  @Test
  void treatsMissingTotalsAndSalaryAsZero() {
    when(configurations.findById("deafult.salary")).thenReturn(Optional.empty());
    assertEquals(
        "Total Spent this month: $0.00\nTotal Credit this month: $0.00\nMonthly income: $0.00\nTotal available this month: $0.00",
        tool.getFinancialSummaryTool(new ExpenseToolInputs.EmptyInput()));
  }
}
