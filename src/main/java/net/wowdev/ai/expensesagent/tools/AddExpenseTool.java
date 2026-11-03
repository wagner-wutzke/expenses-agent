package net.wowdev.ai.expensesagent.tools;

import java.time.LocalDate;
import net.wowdev.ai.expensesagent.model.Expense;
import net.wowdev.ai.expensesagent.model.ExpenseType;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import net.wowdev.ai.expensesagent.service.ChatTrace;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class AddExpenseTool extends AbstractExpenseTool {
  public AddExpenseTool(
      ExpenseRepository expenseRepository, ConfigurationRepository configurationRepository) {
    super(expenseRepository, configurationRepository);
  }

  @Tool(
      description =
          "Log a money transaction. Use DEBIT for spending or bills and CREDIT for incoming money "
              + "such as sales or extra work. Amounts are positive. If direction is unclear, ask "
              + "whether it is DEBIT or CREDIT and do not log until clarified. Save the complete "
              + "description exactly as the user gives it; never shorten, summarize, or paraphrase it.")
  public String addExpenseTool(ExpenseToolInputs.AddExpenseInput input) {
    if (input.type() == null) {
      String result =
          "Is this transaction a DEBIT (money spent) or a CREDIT (money received)? "
              + "I haven't logged it yet.";
      ChatTrace.record("addExpenseTool", input.toString());
      return result;
    }
    LocalDate expenseDate = input.date() == null ? LocalDate.now() : LocalDate.parse(input.date());
    Expense saved =
        expenseRepository.save(
            new Expense(
                null,
                expenseDate,
                input.category(),
                input.description(),
                input.amount(),
                input.type()));
    String result =
        String.format(
            "Success: Added %s$%.2f under category '%s' on %s.",
            saved.getType() == ExpenseType.CREDIT ? "credit " : "debit ",
            saved.getAmount(),
            saved.getCategory(),
            saved.getDate());
    ChatTrace.record("addExpenseTool", input.toString());
    return result;
  }
}
