package net.wowdev.ai.expensesagent.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.YearMonth;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;

class AbstractExpenseToolTest {
  private final ExposedExpenseTool tool =
      new ExposedExpenseTool(mock(ExpenseRepository.class), mock(ConfigurationRepository.class));

  @Test
  void currentMonthRangeSpansTheCurrentCalendarMonth() {
    YearMonth current = YearMonth.now();

    assertEquals(current.atDay(1), tool.currentMonthRange().startDate());
    assertEquals(current.plusMonths(1).atDay(1), tool.currentMonthRange().endDate());
  }

  @Test
  void monthRangeClampsNegativeOffsetsToTheCurrentMonth() {
    YearMonth current = YearMonth.now();

    assertEquals(
        new AbstractExpenseTool.DateRange(current.atDay(1), current.plusMonths(1).atDay(1)),
        tool.monthRange(-2));
  }

  @Test
  void monthRangeUsesCurrentMonthForNullOffset() {
    YearMonth current = YearMonth.now();

    assertEquals(
        new AbstractExpenseTool.DateRange(current.atDay(1), current.plusMonths(1).atDay(1)),
        tool.monthRange(null));
  }

  @Test
  void periodLabelsCoverCurrentPreviousAndEarlierMonths() {
    assertEquals("this month", ExposedExpenseTool.periodLabelFor(null));
    assertEquals("last month", ExposedExpenseTool.periodLabelFor(1));
    assertEquals("3 months ago", ExposedExpenseTool.periodLabelFor(3));
  }

  private static final class ExposedExpenseTool extends AbstractExpenseTool {
    private ExposedExpenseTool(
        ExpenseRepository expenseRepository, ConfigurationRepository configurationRepository) {
      super(expenseRepository, configurationRepository);
    }

    private DateRange currentMonthRange() {
      return currentMonth();
    }

    private DateRange monthRange(Integer monthsAgo) {
      return month(monthsAgo);
    }

    private static String periodLabelFor(Integer monthsAgo) {
      return periodLabel(monthsAgo);
    }
  }
}
