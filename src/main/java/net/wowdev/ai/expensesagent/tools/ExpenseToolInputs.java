package net.wowdev.ai.expensesagent.tools;

import net.wowdev.ai.expensesagent.model.ExpenseType;
import org.springframework.context.annotation.Description;

public final class ExpenseToolInputs {
  private ExpenseToolInputs() {}

  public record AddExpenseInput(
      @Description("The exact numerical transaction amount") Double amount,
      @Description("The category such as Food, Rent, or Side work") String category,
      @Description("A short description of the transaction") String description,
      @Description("Optional transaction date in YYYY-MM-DD format; defaults to today") String date,
      @Description("Required: DEBIT for money spent or CREDIT for money received. Ask if unclear.")
          ExpenseType type) {
    public AddExpenseInput(Double amount, String category, String description, String date) {
      this(amount, category, description, date, ExpenseType.DEBIT);
    }
  }

  public record CategoryInput(
      @Description("The category to look up for the current month") String category) {}

  public record EmptyInput() {}

  public record SetSalaryInput(
      @Description("The requested default monthly salary amount") Double amount,
      @Description("True only after explicit user confirmation to change an existing salary")
          boolean confirmed) {}
}
