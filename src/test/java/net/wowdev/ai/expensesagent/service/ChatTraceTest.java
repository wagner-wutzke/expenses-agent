package net.wowdev.ai.expensesagent.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
