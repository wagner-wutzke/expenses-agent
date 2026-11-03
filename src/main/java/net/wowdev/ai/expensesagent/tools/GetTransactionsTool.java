package net.wowdev.ai.expensesagent.tools;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.wowdev.ai.expensesagent.model.Expense;
import net.wowdev.ai.expensesagent.model.ExpenseType;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import net.wowdev.ai.expensesagent.service.ChatTrace;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class GetTransactionsTool extends AbstractExpenseTool {
  public GetTransactionsTool(
      ExpenseRepository expenseRepository, ConfigurationRepository configurationRepository) {
    super(expenseRepository, configurationRepository);
  }

  @Tool(
      description =
          "Lists itemized DEBIT expenses and/or CREDIT income for a requested calendar month, "
              + "optionally filtered by category. Use type DEBIT for expenses and CREDIT for income; "
              + "leave type empty when the user asks for both. Map this month to monthsAgo 0, last "
              + "month to 1, 2 months ago to 2, and so on. Use this for requests like 'expenses from "
              + "2 months ago', 'incomes from last month', or 'Grocery expenses last month'.")
  public String getTransactions(ExpenseToolInputs.TransactionsInput input) {
    DateRange range = month(input.monthsAgo());
    List<Expense> transactions =
        expenseRepository
            .findByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(
                range.startDate(), range.endDate())
            .stream()
            .filter(e -> input.type() == null || e.getType() == input.type())
            .filter(
                e ->
                    input.category() == null
                        || input.category().isBlank()
                        || e.getCategory().toLowerCase(Locale.ROOT)
                            .contains(input.category().trim().toLowerCase(Locale.ROOT)))
            .toList();
    String result;
    if (transactions.isEmpty()) {
      result = "No matching transactions found for " + periodLabel(input.monthsAgo()) + ".";
    } else {
      String title =
          input.type() == ExpenseType.CREDIT
              ? "Income"
              : input.type() == ExpenseType.DEBIT ? "Expenses" : "Transactions";
      result =
          transactions.stream()
              .map(
                  e ->
                      String.format(
                          "- %s | %s | %s: %s$%.2f (%s)",
                          e.getDate(),
                          e.getCategory(),
                          e.getDescription(),
                          e.getType() == ExpenseType.CREDIT ? "+" : "-",
                          e.getAmount(),
                          e.getType() == ExpenseType.CREDIT ? "credit" : "debit"))
              .collect(
                  Collectors.joining(
                      "\n", title + " for " + periodLabel(input.monthsAgo()) + ":\n", ""));
    }
    ChatTrace.record("getTransactions", input.toString());
    return result;
  }
}
