package net.wowdev.ai.expensesagent.chat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;

class FinancialGuardrailAdvisorTest {
  FinancialGuardrailAdvisor advisor = new FinancialGuardrailAdvisor();
  CallAdvisorChain chain = mock(CallAdvisorChain.class);
  ChatClientRequest request(String text) { return ChatClientRequest.builder().prompt(new Prompt(text)).build(); }

  @Test void forwardsSafeRequestsAndExposesMetadata() {
    ChatClientRequest request = request("Spent $12 on lunch");
    ChatClientResponse response = mock(ChatClientResponse.class);
    when(chain.nextCall(request)).thenReturn(response);
    assertSame(response, advisor.adviseCall(request, chain));
    assertEquals("FinancialGuardrailAdvisor", advisor.getName());
    assertEquals(0, advisor.getOrder());
  }

  @Test void rejectsNegativeTextAndMoneyPatterns() {
    assertThrows(IllegalArgumentException.class, () -> advisor.adviseCall(request("negative expense"), chain));
    assertThrows(IllegalArgumentException.class, () -> advisor.adviseCall(request("spent - $12"), chain));
    assertThrows(IllegalArgumentException.class, () -> advisor.adviseCall(request("spent $-12"), chain));
  }

  @Test void rejectsDatabaseManagementVocabularyCaseInsensitively() {
    assertThrows(SecurityException.class, () -> advisor.adviseCall(request("drop table expenses"), chain));
    assertThrows(SecurityException.class, () -> advisor.adviseCall(request("DELETE FROM expenses"), chain));
  }
}
