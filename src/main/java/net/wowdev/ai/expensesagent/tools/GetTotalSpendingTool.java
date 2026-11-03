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

  @Tool(description = "Calculates the total DEBIT spending for the current calendar month.")
  public String getTotalSpendingTool(ExpenseToolInputs.EmptyInput input) {
    DateRange range = currentMonth();
    Double total = expenseRepository.getTotalSpending(range.startDate(), range.endDate());
    String result = String.format("Total spending this month: $%.2f", total);
    ChatTrace.record("getTotalSpendingTool", input.toString());
    return result;
  }
}
