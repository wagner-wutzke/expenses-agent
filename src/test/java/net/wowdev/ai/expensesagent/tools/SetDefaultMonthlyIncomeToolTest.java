package net.wowdev.ai.expensesagent.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import net.wowdev.ai.expensesagent.model.ConfigurationEntry;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;

class SetDefaultMonthlyIncomeToolTest {
  ConfigurationRepository configurations = mock(ConfigurationRepository.class);
  SetDefaultMonthlyIncomeTool tool = new SetDefaultMonthlyIncomeTool(mock(ExpenseRepository.class), configurations);

  @Test
  void createsSalaryWhenMissingAndRejectsInvalidAmounts() {
    when(configurations.findById("deafult.salary")).thenReturn(Optional.empty());
    when(configurations.save(any(ConfigurationEntry.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    assertEquals("Default monthly salary set to $5000.00.",
        tool.setDefaultMonthlyIncomeTool(new ExpenseToolInputs.SetSalaryInput(5000.0, false), null));
    verify(configurations).save(argThat(entry -> entry.getKey().equals("deafult.salary")
        && entry.getName().equals("Salary") && entry.getValue() == 5000.0));
    assertEquals("Salary must be a positive amount.",
        tool.setDefaultMonthlyIncomeTool(new ExpenseToolInputs.SetSalaryInput(-1.0, false), null));
    assertEquals("Salary must be a positive amount.",
        tool.setDefaultMonthlyIncomeTool(new ExpenseToolInputs.SetSalaryInput(Double.NaN, false), null));
    assertEquals("Salary must be a positive amount.",
        tool.setDefaultMonthlyIncomeTool(new ExpenseToolInputs.SetSalaryInput(null, false), null));
  }

  @Test
  void asksBeforeChangingAndSavesOnlyMatchingConfirmedAmount() {
    ConfigurationEntry existing = new ConfigurationEntry("deafult.salary", "Salary", 4200.0);
    when(configurations.findById("deafult.salary")).thenReturn(Optional.of(existing));
    when(configurations.save(any(ConfigurationEntry.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    ToolContext context = new ToolContext(Map.of("conversationId", "salary-update"));
    assertTrue(tool.setDefaultMonthlyIncomeTool(
        new ExpenseToolInputs.SetSalaryInput(5000.0, false), context).contains("$4200.00 is already set"));
    assertEquals("Default monthly salary updated to $5000.00.",
        tool.setDefaultMonthlyIncomeTool(new ExpenseToolInputs.SetSalaryInput(5000.0, true), context));
    assertEquals(5000.0, existing.getValue());
    verify(configurations).save(existing);
  }

  @Test
  void requiresMatchingAmountAndRejectsUnconfirmedChanges() {
    when(configurations.findById("deafult.salary"))
        .thenReturn(Optional.of(new ConfigurationEntry("deafult.salary", "Salary", 4200.0)));
    ToolContext context = new ToolContext(Map.of("conversationId", "salary-mismatch"));
    assertTrue(tool.setDefaultMonthlyIncomeTool(
        new ExpenseToolInputs.SetSalaryInput(5000.0, true), context).contains("Would you like to change it"));
    assertTrue(tool.setDefaultMonthlyIncomeTool(
        new ExpenseToolInputs.SetSalaryInput(5500.0, true), context).contains("change it to $5500.00"));
    verify(configurations, never()).save(any(ConfigurationEntry.class));
  }
}
