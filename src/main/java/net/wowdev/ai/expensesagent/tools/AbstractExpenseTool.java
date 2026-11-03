package net.wowdev.ai.expensesagent.tools;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
    return month(0);
  }

  protected static DateRange month(Integer monthsAgo) {
    int offset = monthsAgo == null ? 0 : Math.max(0, monthsAgo);
    YearMonth current = YearMonth.now().minusMonths(offset);
    return new DateRange(current.atDay(1), current.plusMonths(1).atDay(1));
  }

  protected static String periodLabel(Integer monthsAgo) {
    int offset = monthsAgo == null ? 0 : Math.max(0, monthsAgo);
    return offset == 0 ? "this month" : offset == 1 ? "last month" : offset + " months ago";
  }

  protected static String calendarMonthLabel(DateRange range) {
    return YearMonth.from(range.startDate())
        .format(DateTimeFormatter.ofPattern("MMMM uuuu", Locale.US));
  }

  protected record DateRange(LocalDate startDate, LocalDate endDate) {}
}
