package net.wowdev.ai.expensesagent.chat;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

class ChatConfigurationTest {
  private ChatConfiguration configuration(String prompt) {
    ChatConfiguration configuration = new ChatConfiguration();
    ReflectionTestUtils.setField(configuration, "systemPrompt", new ByteArrayResource(prompt.getBytes(StandardCharsets.UTF_8)));
    ReflectionTestUtils.setField(configuration, "chatMemoryWindowSize", 30);
    return configuration;
  }

  @Test void buildsClientWhenPromptIsNonblank() {
    ChatClient client =
        configuration("You are helpful")
            .chatClient(
                new StubChatModel(),
                new FinancialGuardrailAdvisor(),
                ToolCallbackProvider.from());
    assertNotNull(client);
  }

  @Test void createsMemoryWindowWithConfiguredMessageLimit() {
    ChatConfiguration configuration = configuration("You are helpful");
    ReflectionTestUtils.setField(configuration, "chatMemoryWindowSize", 2);
    ChatMemory chatMemory = configuration.chatMemory();

    chatMemory.add(
        "window-test",
        java.util.List.of(new UserMessage("first"), new UserMessage("second"), new UserMessage("third")));

    assertEquals(2, chatMemory.get("window-test").size());
    assertEquals("second", chatMemory.get("window-test").get(0).getText());
    assertEquals("third", chatMemory.get("window-test").get(1).getText());
  }

  @Test void rejectsMissingAndBlankPrompt() {
    ChatConfiguration missing = new ChatConfiguration();
    assertThrows(
        IllegalStateException.class,
        () ->
            missing.chatClient(
                new StubChatModel(),
                new FinancialGuardrailAdvisor(),
                ToolCallbackProvider.from()));
    assertThrows(
        IllegalStateException.class,
        () ->
            configuration(" \n ")
                .chatClient(
                    new StubChatModel(),
                    new FinancialGuardrailAdvisor(),
                    ToolCallbackProvider.from()));
  }

  @Test void wrapsPromptReadFailure() {
    ChatConfiguration configuration = new ChatConfiguration();
    ReflectionTestUtils.setField(configuration, "systemPrompt", new ByteArrayResource(new byte[0]) {
      @Override public String getContentAsString(java.nio.charset.Charset charset) throws IOException { throw new IOException("broken"); }
      @Override public String getDescription() { return "broken prompt"; }
    });
    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () ->
            configuration.chatClient(
                new StubChatModel(),
                new FinancialGuardrailAdvisor(),
                ToolCallbackProvider.from()));
    assertTrue(exception.getMessage().contains("broken prompt"));
    assertInstanceOf(IOException.class, exception.getCause());
  }

  private static class StubChatModel implements ChatModel {
    @Override public org.springframework.ai.chat.model.ChatResponse call(org.springframework.ai.chat.prompt.Prompt prompt) { return null; }
  }
}
