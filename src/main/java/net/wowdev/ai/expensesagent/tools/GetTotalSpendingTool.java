package net.wowdev.ai.expensesagent.tools;

import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import net.wowdev.ai.expensesagent.service.ChatTrace;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class GetTotalSpendingTool extends AbstractExpenseTool {
  public GetTotalSpendingTool(
      ExpenseRepository expenseRepository, ConfigurationRepository configurationRepository) {
    super(expenseRepository, configurationRepository);
  }

  @Tool(description = "Calculates total DEBIT spending for a requested calendar month. Interpret last month as monthsAgo = 1, 2 months ago as 2, etc. Use 0 if no month is mentioned.")
  public String getTotalSpendingTool(ExpenseToolInputs.PeriodInput input) {
    DateRange range = month(input.monthsAgo());
    Double total = expenseRepository.getTotalSpending(range.startDate(), range.endDate());
    String result =
        String.format(
            "Total spending for %s: $%.2f",
            calendarMonthLabel(range), total == null ? 0.0 : total);
    ChatTrace.record("getTotalSpendingTool", input.toString());
    return result;
  }

  public String getTotalSpendingTool(ExpenseToolInputs.EmptyInput input) {
    return getTotalSpendingTool(new ExpenseToolInputs.PeriodInput(0));
  }
}
