package net.wowdev.ai.expensesagent.tools;

import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.wowdev.ai.expensesagent.model.ConfigurationEntry;
import net.wowdev.ai.expensesagent.repository.ConfigurationRepository;
import net.wowdev.ai.expensesagent.repository.ExpenseRepository;
import net.wowdev.ai.expensesagent.service.ChatTrace;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class SetDefaultMonthlyIncomeTool extends AbstractExpenseTool {
  private static final String DEFAULT_SALARY_KEY = "deafult.salary";
  private static final String SALARY_NAME = "Salary";
  private static final long CONFIRMATION_TTL_SECONDS = 900;
  private final ConcurrentMap<String, PendingSalaryChange> pendingSalaryChanges =
      new ConcurrentHashMap<>();

  public SetDefaultMonthlyIncomeTool(
      ExpenseRepository expenseRepository, ConfigurationRepository configurationRepository) {
    super(expenseRepository, configurationRepository);
  }

  @Tool(
      description =
          "Sets the user's default monthly salary. If one already exists, ask whether the user wants"
              + " to change it and wait for explicit confirmation before updating.")
  public String setDefaultMonthlyIncomeTool(
      ExpenseToolInputs.SetSalaryInput input, ToolContext toolContext) {
    if (input.amount() == null || !Double.isFinite(input.amount()) || input.amount() <= 0) {
      return "Salary must be a positive amount.";
    }

    String conversationId =
        toolContext == null || toolContext.getContext().get("conversationId") == null
            ? "default"
            : toolContext.getContext().get("conversationId").toString();
    Instant now = Instant.now();
    PendingSalaryChange pending = pendingSalaryChanges.get(conversationId);
    if (pending != null
        && pending.createdAt().plusSeconds(CONFIRMATION_TTL_SECONDS).isBefore(now)) {
      pendingSalaryChanges.remove(conversationId, pending);
      pending = null;
    }

    ConfigurationEntry existing = configurationRepository.findById(DEFAULT_SALARY_KEY).orElse(null);
    String result;
    if (existing == null) {
      configurationRepository.save(
          new ConfigurationEntry(DEFAULT_SALARY_KEY, SALARY_NAME, input.amount()));
      pendingSalaryChanges.remove(conversationId);
      result = String.format(Locale.US, "Default monthly salary set to $%.2f.", input.amount());
    } else if (pending != null
        && input.confirmed()
        && Double.compare(pending.amount(), input.amount()) == 0) {
      existing.setValue(pending.amount());
      configurationRepository.save(existing);
      pendingSalaryChanges.remove(conversationId, pending);
      result =
          String.format(Locale.US, "Default monthly salary updated to $%.2f.", pending.amount());
    } else {
      pendingSalaryChanges.put(conversationId, new PendingSalaryChange(input.amount(), now));
      result =
          String.format(
              Locale.US,
              "A default monthly salary of $%.2f is already set. Would you like to change it "
                  + "to $%.2f per month? Ask the user to confirm this amount.",
              existing.getValue(),
              input.amount());
    }
    ChatTrace.record("setDefaultMonthlyIncomeTool", input.toString());
    return result;
  }

  private record PendingSalaryChange(Double amount, Instant createdAt) {}
}
