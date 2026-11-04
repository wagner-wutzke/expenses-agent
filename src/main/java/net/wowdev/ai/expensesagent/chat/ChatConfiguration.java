package net.wowdev.ai.expensesagent.chat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import net.wowdev.ai.expensesagent.service.ChatTrace;

@Configuration
public class ChatConfiguration {

  @Value("${agent.system-prompt-file}")
  private Resource systemPrompt;

  @Value("${agent.chat.memory.window.size:30}")
  private int chatMemoryWindowSize;

  @Bean
  public ChatMemory chatMemory() {
    return MessageWindowChatMemory.builder()
        .chatMemoryRepository(new InMemoryChatMemoryRepository())
        .maxMessages(chatMemoryWindowSize)
        .build();
  }

  @Bean
  public ChatClient chatClient(
      ChatModel chatModel,
      FinancialGuardrailAdvisor financialGuardrailAdvisor,
      ToolCallbackProvider mcpToolCallbackProvider) {

    validateSystemPrompt();

    return ChatClient.builder(chatModel)
        .defaultSystem(systemPrompt, StandardCharsets.UTF_8)
        .defaultAdvisors(
            new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE),
            MessageChatMemoryAdvisor.builder(chatMemory()).build(),
            financialGuardrailAdvisor)
        .defaultTools(
            ToolCallbackProvider.from(
                Arrays.stream(mcpToolCallbackProvider.getToolCallbacks())
                    .map(ChatConfiguration::traceToolCallback)
                    .toList()))
        .build();
  }

  private static ToolCallback traceToolCallback(ToolCallback delegate) {
    return new ToolCallback() {
      @Override
      public org.springframework.ai.tool.definition.ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
      }

      @Override
      public org.springframework.ai.tool.metadata.ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
      }

      @Override
      public String call(String toolInput) {
        ChatTrace.record("function", delegate.getToolDefinition().name(), toolInput);
        return delegate.call(toolInput);
      }

      @Override
      public String call(String toolInput, ToolContext toolContext) {
        ChatTrace.record("function", delegate.getToolDefinition().name(), toolInput);
        return delegate.call(toolInput, toolContext);
      }
    };
  }

  private void validateSystemPrompt() {
    try {
      if (systemPrompt == null) {
        throw new IllegalStateException("No system prompt file has been provided");
      }
      if (systemPrompt.getContentAsString(StandardCharsets.UTF_8).isBlank()) {
        throw new IllegalStateException("The system prompt file is empty.");
      }
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Could not read the system prompt file: " + systemPrompt.getDescription(), exception);
    }
  }
}
