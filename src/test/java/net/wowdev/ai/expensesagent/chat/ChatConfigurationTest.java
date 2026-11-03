package net.wowdev.ai.expensesagent.chat;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

class ChatConfigurationTest {
  private ChatConfiguration configuration(String prompt) {
    ChatConfiguration configuration = new ChatConfiguration();
    ReflectionTestUtils.setField(configuration, "systemPrompt", new ByteArrayResource(prompt.getBytes(StandardCharsets.UTF_8)));
    return configuration;
  }

  @Test void buildsClientWhenPromptIsNonblank() {
    ChatClient client = configuration("You are helpful").chatClient(new StubChatModel(), new FinancialGuardrailAdvisor());
    assertNotNull(client);
  }

  @Test void rejectsMissingAndBlankPrompt() {
    ChatConfiguration missing = new ChatConfiguration();
    assertThrows(IllegalStateException.class, () -> missing.chatClient(new StubChatModel(), new FinancialGuardrailAdvisor()));
    assertThrows(IllegalStateException.class, () -> configuration(" \n ").chatClient(new StubChatModel(), new FinancialGuardrailAdvisor()));
  }

  @Test void wrapsPromptReadFailure() {
    ChatConfiguration configuration = new ChatConfiguration();
    ReflectionTestUtils.setField(configuration, "systemPrompt", new ByteArrayResource(new byte[0]) {
      @Override public String getContentAsString(java.nio.charset.Charset charset) throws IOException { throw new IOException("broken"); }
      @Override public String getDescription() { return "broken prompt"; }
    });
    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> configuration.chatClient(new StubChatModel(), new FinancialGuardrailAdvisor()));
    assertTrue(exception.getMessage().contains("broken prompt"));
    assertInstanceOf(IOException.class, exception.getCause());
  }

  private static class StubChatModel implements ChatModel {
    @Override public org.springframework.ai.chat.model.ChatResponse call(org.springframework.ai.chat.prompt.Prompt prompt) { return null; }
  }
}
