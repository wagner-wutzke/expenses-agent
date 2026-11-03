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
          "Returns a requested month's summary with total DEBIT spending, total CREDIT income, configured "
              + "default salary as Monthly income, and available balance (credits plus Monthly "
              + "income minus spending). Accept monthsAgo (0 this month, 1 last month, 2 two months ago, etc.; use 0 when omitted). Always report every line returned.")
  public String getFinancialSummaryTool(ExpenseToolInputs.PeriodInput input) {
    DateRange range = month(input.monthsAgo());
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
                Total Spent %s: $%.2f
                Total Credit %s: $%.2f
                Monthly income: $%.2f
                Total available %s: $%.2f
                """,
                periodLabel(input.monthsAgo()), spending, periodLabel(input.monthsAgo()), credits, configuredSalary, periodLabel(input.monthsAgo()), credits + configuredSalary - spending)
            .stripTrailing();
    ChatTrace.record("getFinancialSummaryTool", input.toString());
    return result;
  }

  public String getFinancialSummaryTool(ExpenseToolInputs.EmptyInput input) {
    return getFinancialSummaryTool(new ExpenseToolInputs.PeriodInput(0));
  }
}
