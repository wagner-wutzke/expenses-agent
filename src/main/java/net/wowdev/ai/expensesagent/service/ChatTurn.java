package net.wowdev.ai.expensesagent.service;

/** The observable details for one assistant reply. */
public record ChatTurn(String prompt, String reasoning, String response, TokenUsage tokenUsage) {}
