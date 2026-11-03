package net.wowdev.ai.expensesagent.tools;

import java.time.LocalDate;
import net.wowdev.ai.expensesagent.model.Expense;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import net.wowdev.ai.expensesagent.service.ChatTrace;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class UpdateExpenseTool extends AbstractExpenseTool {
  public UpdateExpenseTool(
      ExpenseRepository expenseRepository, ConfigurationRepository configurationRepository) {
    super(expenseRepository, configurationRepository);
  }

  @Tool(
      description =
          "Updates an existing expense or income transaction by its ID. First use an itemized "
              + "transaction lookup to identify the ID. Only change fields the user requested; "
              + "omit all other fields to preserve their current values. Preserve a supplied "
              + "description completely and verbatim.")
  public String updateExpense(ExpenseToolInputs.UpdateExpenseInput input) {
    if (input.id() == null) {
      return "A transaction ID is required to update a record.";
    }
    if (input.amount() == null
        && input.category() == null
        && input.description() == null
        && input.date() == null
        && input.type() == null) {
      return "No changes were provided for transaction ID " + input.id() + ".";
    }

    Expense expense = expenseRepository.findById(input.id()).orElse(null);
    if (expense == null) {
      return "No transaction found with ID " + input.id() + ".";
    }
    if (input.amount() != null) expense.setAmount(input.amount());
    if (input.category() != null) expense.setCategory(input.category());
    if (input.description() != null) expense.setDescription(input.description());
    if (input.date() != null) expense.setDate(LocalDate.parse(input.date()));
    if (input.type() != null) expense.setType(input.type());

    expenseRepository.save(expense);
    String result = "Updated transaction ID " + expense.getId() + ".";
    ChatTrace.record("updateExpense", input.toString());
    return result;
  }
}
