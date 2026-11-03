package net.wowdev.ai.expensesagent.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ExpenseTest {
  @Test
  void defaultsNullTypeToDebitBeforePersistence() {
    Expense expense = new Expense();
    expense.setType(null);

    expense.defaultType();

    assertEquals(ExpenseType.DEBIT, expense.getType());
  }

  @Test
  void constructorsAndAccessorsExposeAllFields() {
    LocalDate date = LocalDate.of(2026, 1, 2);
    Expense expense = new Expense(7L, date, "Food", "Lunch", 12.5);
    assertEquals(7L, expense.getId());
    assertEquals(date, expense.getDate());
    assertEquals("Food", expense.getCategory());
    assertEquals("Lunch", expense.getDescription());
    assertEquals(12.5, expense.getAmount());
    assertEquals(ExpenseType.DEBIT, expense.getType());
    expense.setId(8L);
    expense.setDate(date.plusDays(1));
    expense.setCategory("Travel");
    expense.setDescription("Train");
    expense.setAmount(20.0);
    expense.setType(ExpenseType.CREDIT);
    assertEquals(8L, expense.getId());
    assertEquals("Travel", expense.getCategory());
    assertEquals(20.0, expense.getAmount());
    assertEquals(ExpenseType.CREDIT, expense.getType());
    Expense empty = new Expense();
    empty.setDescription("test");
    assertEquals("test", empty.getDescription());
  }
}
