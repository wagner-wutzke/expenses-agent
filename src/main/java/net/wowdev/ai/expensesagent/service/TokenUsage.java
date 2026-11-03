package net.wowdev.ai.expensesagent.service;

/** Token counts reported by the chat model for a response. */
public record TokenUsage(Integer inputTokens, Integer outputTokens, Integer totalTokens) {
  public static TokenUsage unavailable() {
    return new TokenUsage(null, null, null);
  }
}
