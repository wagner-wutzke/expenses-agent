package net.wowdev.ai.expensesagent.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import net.wowdev.ai.expensesagent.model.ExpenseType;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;

class AddExpenseToolTest {
  ExpenseRepository expenses = mock(ExpenseRepository.class);
  AddExpenseTool tool = new AddExpenseTool(expenses, mock(ConfigurationRepository.class));

  @Test
  void savesDebitAtProvidedDate() {
    LocalDate date = LocalDate.of(2025, 3, 4);
    when(expenses.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    assertEquals(
        "Success: Added debit $12.50 under category 'Food' on 2025-03-04.",
        tool.addExpenseTool(
            new ExpenseToolInputs.AddExpenseInput(12.5, "Food", "Lunch", date.toString(), ExpenseType.DEBIT)));
    verify(expenses).save(argThat(e -> e.getType() == ExpenseType.DEBIT && e.getDate().equals(date)));
  }

  @Test
  void defaultsDateToTodayAndSavesCredit() {
    when(expenses.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    assertEquals(
        "Success: Added credit $300.00 under category 'Side work' on " + LocalDate.now() + ".",
        tool.addExpenseTool(
            new ExpenseToolInputs.AddExpenseInput(300.0, "Side work", "Weekend job", null, ExpenseType.CREDIT)));
    verify(expenses).save(argThat(e -> e.getType() == ExpenseType.CREDIT));
  }

  @Test
  void asksTypeAndDoesNotSaveWhenUnclear() {
    assertEquals(
        "Is this transaction a DEBIT (money spent) or a CREDIT (money received)? I haven't logged it yet.",
        tool.addExpenseTool(
            new ExpenseToolInputs.AddExpenseInput(40.0, "Other", "Transaction", null, null)));
    verify(expenses, never()).save(any());
  }
}
