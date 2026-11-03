package net.wowdev.ai.expensesagent.tools;

import java.util.List;
import java.util.stream.Collectors;
import net.wowdev.ai.expensesagent.model.ExpenseType;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import net.wowdev.ai.expensesagent.service.ChatTrace;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class GetExpensesByCategoryTool extends AbstractExpenseTool {
  public GetExpensesByCategoryTool(
      ExpenseRepository expenseRepository, ConfigurationRepository configurationRepository) {
    super(expenseRepository, configurationRepository);
  }

  @Tool(description = "Retrieves this month's itemized transactions matching a specific category.")
  public String getExpensesByCategoryTool(ExpenseToolInputs.CategoryInput input) {
    DateRange range = currentMonth();
    List<net.wowdev.ai.expensesagent.model.Expense> expenses =
        expenseRepository.findByCategoryIgnoreCaseAndDateGreaterThanEqualAndDateLessThan(
            input.category(), range.startDate(), range.endDate());
    if (expenses.isEmpty()) {
      String result = "No expenses found for category: " + input.category();
      ChatTrace.record("getExpensesByCategoryTool", input.toString());
      return result;
    }
    String result =
        expenses.stream()
            .map(
                expense ->
                    String.format(
                        "- %s: %s$%.2f (%s)",
                        expense.getDate(),
                        expense.getType() == ExpenseType.CREDIT ? "+" : "-",
                        expense.getAmount(),
                        expense.getType() == ExpenseType.CREDIT ? "credit" : "debit"))
            .collect(Collectors.joining("\n", "Expenses for " + input.category() + ":\n", ""));
    ChatTrace.record("getExpensesByCategoryTool", input.toString());
    return result;
  }
}
