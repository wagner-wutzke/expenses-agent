package net.wowdev.ai.expensesagent.chat;

import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.stereotype.Component;

@Component
public class FinancialGuardrailAdvisor implements CallAdvisor {

  // Regex pattern to catch explicit negative spending symbols or text phrases
  private static final Pattern NEGATIVE_MONEY_PATTERN = Pattern.compile("(-\\s*\\$?|\\$-\\s*)\\d+");

  @Override
  public @NonNull ChatClientResponse adviseCall(
      ChatClientRequest chatClientRequest, @NonNull CallAdvisorChain callAdvisorChain) {
    // Extract the raw user instruction from the unsealed request payload
    String userText = chatClientRequest.prompt().getUserMessage().getText();

    if (userText != null) {
      // Guardrail Clause 1: Structural string validation
      if (NEGATIVE_MONEY_PATTERN.matcher(userText).find()
          || userText.toLowerCase().contains("negative")) {
        throw new IllegalArgumentException(
            "Guardrail Blocked Action: Financial logging requests cannot process negative balances "
                + "or negative spending costs.");
      }

      // Guardrail Clause 2: SQL Injection mitigation
      String upperText = userText.toUpperCase();
      if (upperText.contains("DROP TABLE") || upperText.contains("DELETE FROM")) {
        throw new SecurityException(
            "Guardrail Blocked Action: Unsanitized database management vocabulary detected.");
      }
    }

    // Pass validation -> Forward execution down the Spring AI 2.0 pipeline
    return callAdvisorChain.nextCall(chatClientRequest);
  }

  @Override
  public @NonNull String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public int getOrder() {
    // Execution priority set to 0 to intercept before LLM token generation kicks off
    return 0;
  }
}
