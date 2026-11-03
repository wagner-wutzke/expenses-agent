package net.wowdev.ai.expensesagent.tools;

import net.wowdev.ai.expensesagent.model.ExpenseType;
import org.springframework.context.annotation.Description;
import org.springframework.ai.tool.annotation.ToolParam;

public final class ExpenseToolInputs {
  private ExpenseToolInputs() {}

  public record AddExpenseInput(
      @Description("The exact numerical transaction amount") Double amount,
      @Description("The category such as Food, Rent, or Side work") String category,
      @Description("The complete transaction description as stated by the user. Preserve it verbatim: do not shorten, summarize, paraphrase, or omit any details.") String description,
      @Description("Optional transaction date in YYYY-MM-DD format; defaults to today") String date,
      @Description("Required: DEBIT for money spent or CREDIT for money received. Ask if unclear.")
          ExpenseType type) {
    public AddExpenseInput(Double amount, String category, String description, String date) {
      this(amount, category, description, date, ExpenseType.DEBIT);
    }
  }

  public record CategoryInput(
      @Description("The category to look up") String category,
      @Description("Number of months before the current month to query: 0 for this month, 1 for last month, 2 for two months ago. Use 0 if no month was requested.") Integer monthsAgo) {
    public CategoryInput(String category) {
      this(category, 0);
    }
  }

  public record PeriodInput(
      @Description("Number of months before the current month to query: 0 for this month, 1 for last month, 2 for two months ago, and so on. Use 0 if no month was requested.") Integer monthsAgo) {
    public PeriodInput() {
      this(0);
    }
  }

  public record TransactionsInput(
      @Description("Optional transaction type filter: DEBIT for spending or CREDIT for incoming money. Leave empty for both.") ExpenseType type,
      @Description("Optional category filter, such as Grocery or Transport") String category,
      @Description("Number of months before the current month: 0 this month, 1 last month, 2 two months ago, etc. Use 0 when no month is mentioned.") Integer monthsAgo) {}

  public record UpdateExpenseInput(
      @ToolParam(required = true, description = "ID of the existing transaction to update") Long id,
      @ToolParam(required = false, description = "New amount; omit to keep the existing amount") Double amount,
      @ToolParam(required = false, description = "New category; omit to keep the existing category") String category,
      @ToolParam(required = false, description = "New complete description; omit to keep the existing description") String description,
      @ToolParam(required = false, description = "New date in YYYY-MM-DD format; omit to keep the existing date") String date,
      @ToolParam(required = false, description = "New type DEBIT or CREDIT; omit to keep the existing type") ExpenseType type) {}

  public record DeleteExpenseInput(
      @Description("ID of the transaction to delete") Long id,
      @Description("True only after the user explicitly confirms deleting this exact transaction")
          boolean confirmed) {}

  public record EmptyInput() {}

  public record SetSalaryInput(
      @Description("The requested default monthly salary amount") Double amount,
      @Description("True only after explicit user confirmation to change an existing salary")
          boolean confirmed) {}
}
