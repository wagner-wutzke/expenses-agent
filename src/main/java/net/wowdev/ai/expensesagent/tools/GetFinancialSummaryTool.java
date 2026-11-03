package net.wowdev.ai.expensesagent.tools;

import net.wowdev.ai.expensesagent.model.ConfigurationEntry;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import net.wowdev.ai.expensesagent.service.ChatTrace;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class GetFinancialSummaryTool extends AbstractExpenseTool {
  private static final String DEFAULT_SALARY_KEY = "deafult.salary";

  public GetFinancialSummaryTool(
      ExpenseRepository expenseRepository, ConfigurationRepository configurationRepository) {
    super(expenseRepository, configurationRepository);
  }

  @Tool(
      description =
          "Returns this month's summary with total DEBIT spending, total CREDIT income, configured "
              + "default salary as Monthly income, and available balance (credits plus Monthly "
              + "income minus spending). Always report every line returned.")
  public String getFinancialSummaryTool(ExpenseToolInputs.EmptyInput input) {
    DateRange range = currentMonth();
    Double totalSpent = expenseRepository.getTotalSpending(range.startDate(), range.endDate());
    Double totalCredits = expenseRepository.getTotalCredits(range.startDate(), range.endDate());
    Double configuredSalary =
        configurationRepository.findById(DEFAULT_SALARY_KEY)
            .map(ConfigurationEntry::getValue)
            .orElse(0.0);
    double spending = totalSpent == null ? 0.0 : totalSpent;
    double credits = totalCredits == null ? 0.0 : totalCredits;
    String result =
        String.format(
                """
                Total Spent this month: $%.2f
                Total Credit this month: $%.2f
                Monthly income: $%.2f
                Total available this month: $%.2f
                """,
                spending, credits, configuredSalary, credits + configuredSalary - spending)
            .stripTrailing();
    ChatTrace.record("getFinancialSummaryTool", input.toString());
    return result;
  }
}
