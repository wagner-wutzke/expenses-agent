package net.wowdev.ai.expensesagent.service;

import java.util.ArrayList;
import java.util.List;
import net.wowdev.ai.expensesagent.model.Expense;

/** Captures observable finance-tool activity during one synchronous chat request. */
public final class ChatTrace implements AutoCloseable {
  private static final ThreadLocal<List<Event>> ACTIVE = new ThreadLocal<>();
  private final List<Event> events;

  private ChatTrace() {
    this.events = new ArrayList<>();
    ACTIVE.set(events);
  }

  public static ChatTrace start() {
    return new ChatTrace();
  }

  public static void record(String tool, String input) {
    record("function", tool, input);
  }

  public static void record(String type, String tool, String input) {
    List<Event> events = ACTIVE.get();
    if (events != null) {
      events.add(new Event(type, tool, input, List.of()));
    }
  }

  public static void recordTransactions(String tool, String input, List<Expense> expenses) {
    List<Event> events = ACTIVE.get();
    if (events != null) {
      List<TransactionDetails> transactions =
          expenses.stream()
              .map(
                  expense ->
                      new TransactionDetails(
                          expense.getId(),
                          expense.getDate(),
                          expense.getCategory(),
                          expense.getDescription(),
                          expense.getAmount(),
                          expense.getType()))
              .toList();
      events.add(new Event("function", tool, input, transactions));
    }
  }

  public List<TransactionDetails> transactionDetails() {
    if (events.stream().anyMatch(event -> event.tool().equals("updateExpense"))) {
      return List.of();
    }
    return events.stream().flatMap(event -> event.transactions().stream()).distinct().toList();
  }

  public String reasoningSummary() {
    if (events.isEmpty()) {
      return "No expense tools were called. The assistant used your prompt and conversation context to respond.";
    }
    StringBuilder summary = new StringBuilder();
    for (Event event : events) {
      if (!summary.isEmpty()) {
        summary.append("\n\n");
      }
      summary
          .append("Type: ").append(event.type())
          .append("\nAction: ").append(event.tool())
          .append("\nInput: ").append(event.input());
    }
    return summary.toString();
  }

  @Override
  public void close() {
    ACTIVE.remove();
  }

  private record Event(String type, String tool, String input, List<TransactionDetails> transactions) {}
}
