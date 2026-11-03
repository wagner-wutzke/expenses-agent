package net.wowdev.ai.expensesagent.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.wowdev.ai.expensesagent.model.ExpenseType;
import org.junit.jupiter.api.Test;

class ExpenseToolInputsTest {
  @Test
  void defaultsLegacyExpenseInputToDebit() {
    ExpenseToolInputs.AddExpenseInput input =
        new ExpenseToolInputs.AddExpenseInput(12.5, "Food", "Lunch", "2026-11-02");

    assertEquals(12.5, input.amount());
    assertEquals("Food", input.category());
    assertEquals("Lunch", input.description());
    assertEquals("2026-11-02", input.date());
    assertEquals(ExpenseType.DEBIT, input.type());
  }

  @Test
  void preservesExplicitExpenseType() {
    ExpenseToolInputs.AddExpenseInput input =
        new ExpenseToolInputs.AddExpenseInput(300.0, "Side work", "Extra job", null, ExpenseType.CREDIT);

    assertEquals(ExpenseType.CREDIT, input.type());
  }

  @Test
  void constructsCategoryEmptyAndSalaryInputs() {
    assertEquals("Rent", new ExpenseToolInputs.CategoryInput("Rent").category());
    assertEquals(new ExpenseToolInputs.EmptyInput(), new ExpenseToolInputs.EmptyInput());
    ExpenseToolInputs.SetSalaryInput salary = new ExpenseToolInputs.SetSalaryInput(5000.0, true);
    assertEquals(5000.0, salary.amount());
    assertEquals(true, salary.confirmed());
  }
}
