package net.wowdev.ai.expensesagent.tools;

import java.util.List;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import net.wowdev.ai.expensesagent.service.ChatTrace;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class GetSpendingBreakdownTool extends AbstractExpenseTool {
  public GetSpendingBreakdownTool(
      ExpenseRepository expenseRepository, ConfigurationRepository configurationRepository) {
    super(expenseRepository, configurationRepository);
  }

  @Tool(
      description =
          "Retrieves an aggregate breakdown of DEBIT spending by category for the current calendar "
              + "month. Use this for charts, graphs, breakdowns, or visual summaries.")
  public String getSpendingBreakdown() {
    DateRange range = currentMonth();
    List<Object[]> data =
        expenseRepository.getSpendingPerCategory(range.startDate(), range.endDate());
    if (data.isEmpty()) {
      String result = "No spending data available for this month to construct a breakdown chart.";
      ChatTrace.record("getSpendingBreakdown", "{}");
      return result;
    }

    StringBuilder result =
        new StringBuilder("Spending Category Allocation Chart Data (this month):\n");
    result.append("| Category | Total Spent ($) |\n|---|---|\n");
    for (Object[] row : data) {
      String category = (String) row[0];
      Double total = (Double) row[1];
      result.append(String.format("| %s | %.2f |\n", category, total));
    }
    String output = result.toString();
    ChatTrace.record("getSpendingBreakdown", "{}");
    return output;
  }
}
