package net.wowdev.ai.expensesagent.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;

class GetTotalSpendingToolTest {
  ExpenseRepository expenses = mock(ExpenseRepository.class);
  GetTotalSpendingTool tool = new GetTotalSpendingTool(expenses, mock(ConfigurationRepository.class));

  @Test
  void reportsCurrentMonthDebitTotal() {
    when(expenses.getTotalSpending(any(LocalDate.class), any(LocalDate.class))).thenReturn(42.5);
    String month = YearMonth.now().format(DateTimeFormatter.ofPattern("MMMM uuuu", Locale.US));
    assertEquals("Total spending for " + month + ": $42.50",
        tool.getTotalSpendingTool(new ExpenseToolInputs.EmptyInput()));
    verify(expenses).getTotalSpending(any(LocalDate.class), any(LocalDate.class));
  }
}
