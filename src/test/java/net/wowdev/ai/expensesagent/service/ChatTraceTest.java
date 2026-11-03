package net.wowdev.ai.expensesagent.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import net.wowdev.ai.expensesagent.model.Expense;
import net.wowdev.ai.expensesagent.model.ExpenseType;
import org.junit.jupiter.api.Test;

class ChatTraceTest {
  @Test
  void capturesOnlyToolActionsAndInputsForTheCurrentCall() {
    ChatTrace.record("outside", "ignored");
    try (ChatTrace trace = ChatTrace.start()) {
      ChatTrace.record("getTotalSpendingTool", "EmptyInput[]");
      ChatTrace.record("getExpensesByCategoryTool", "CategoryInput[category=Food]");
      assertEquals(
          "Action: getTotalSpendingTool\nInput: EmptyInput[]\n\n"
              + "Action: getExpensesByCategoryTool\nInput: CategoryInput[category=Food]",
          trace.reasoningSummary());
    }
    ChatTrace.record("after-close", "ignored");
  }

  @Test
  void capturesItemizedTransactionDetailsForTheFrontend() {
    Expense expense =
        new Expense(42L, LocalDate.of(2026, 11, 2), "Groceries", "Market fruit", 18.75, ExpenseType.DEBIT);
    try (ChatTrace trace = ChatTrace.start()) {
      ChatTrace.recordTransactions("getTransactions", "type=DEBIT", List.of(expense));

      assertEquals(
          List.of(
              new TransactionDetails(
                  42L,
                  LocalDate.of(2026, 11, 2),
                  "Groceries",
                  "Market fruit",
                  18.75,
                  ExpenseType.DEBIT)),
          trace.transactionDetails());
    }
  }
}
