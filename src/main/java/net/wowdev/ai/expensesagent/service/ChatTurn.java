package net.wowdev.ai.expensesagent.service;

import java.util.List;

/** The observable details for one assistant reply. */
public record ChatTurn(
    String prompt,
    String reasoning,
    String response,
    TokenUsage tokenUsage,
    List<TransactionDetails> transactions) {
  public ChatTurn(String prompt, String reasoning, String response, TokenUsage tokenUsage) {
    this(prompt, reasoning, response, tokenUsage, List.of());
  }

  public ChatTurn {
    transactions = transactions == null ? List.of() : List.copyOf(transactions);
  }
}
