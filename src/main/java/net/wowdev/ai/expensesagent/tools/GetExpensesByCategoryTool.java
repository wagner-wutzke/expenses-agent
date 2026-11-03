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

  @Tool(description = "Retrieves itemized transactions matching a category for the requested calendar month. Interpret month requests such as last month, 2 months ago, or 3 months ago as monthsAgo = 1, 2, or 3. If no month is mentioned, use 0 for this month. Includes both DEBIT expenses and CREDIT income. Correct obvious misspellings in the category using the conversation context (for example, 'Gorcery' means 'Grocery').")
  public String getExpensesByCategoryTool(ExpenseToolInputs.CategoryInput input) {
    DateRange range = month(input.monthsAgo());
    List<net.wowdev.ai.expensesagent.model.Expense> expenses =
        expenseRepository.findByCategoryIgnoreCaseAndDateGreaterThanEqualAndDateLessThan(
            input.category(), range.startDate(), range.endDate());
    if (expenses.isEmpty()) {
      String result = "No expenses found for category " + input.category() + " in " + periodLabel(input.monthsAgo()) + ".";
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
            .collect(Collectors.joining("\n", "Transactions for " + input.category() + " (" + periodLabel(input.monthsAgo()) + "):\n", ""));
    ChatTrace.record("getExpensesByCategoryTool", input.toString());
    return result;
  }
}
