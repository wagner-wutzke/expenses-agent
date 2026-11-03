package net.wowdev.ai.expensesagent.service;

import java.time.LocalDate;
import net.wowdev.ai.expensesagent.model.ExpenseType;

public record TransactionDetails(
    Long id, LocalDate date, String category, String description, Double amount, ExpenseType type) {}
