package net.wowdev.ai.expensesagent.service;

import java.util.ArrayList;
import java.util.List;

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
    List<Event> events = ACTIVE.get();
    if (events != null) {
      events.add(new Event(tool, input));
    }
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
      summary.append("Action: ").append(event.tool()).append("\nInput: ").append(event.input());
    }
    return summary.toString();
  }

  @Override
  public void close() {
    ACTIVE.remove();
  }

  private record Event(String tool, String input) {}
}
