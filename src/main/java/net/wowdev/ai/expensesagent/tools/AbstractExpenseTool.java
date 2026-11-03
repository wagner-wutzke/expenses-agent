package net.wowdev.ai.expensesagent.tools;

import java.time.LocalDate;
import java.time.YearMonth;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;

public abstract class AbstractExpenseTool {
  protected final ExpenseRepository expenseRepository;
  protected final ConfigurationRepository configurationRepository;

  protected AbstractExpenseTool(
      ExpenseRepository expenseRepository, ConfigurationRepository configurationRepository) {
    this.expenseRepository = expenseRepository;
    this.configurationRepository = configurationRepository;
  }

  protected static DateRange currentMonth() {
    YearMonth current = YearMonth.now();
    return new DateRange(current.atDay(1), current.plusMonths(1).atDay(1));
  }

  protected record DateRange(LocalDate startDate, LocalDate endDate) {}
}
