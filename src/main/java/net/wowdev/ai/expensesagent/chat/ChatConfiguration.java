package net.wowdev.ai.expensesagent.chat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;

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
      ChatModel chatModel, FinancialGuardrailAdvisor financialGuardrailAdvisor) {

    validateSystemPrompt();

    return ChatClient.builder(chatModel)
        .defaultSystem(systemPrompt, StandardCharsets.UTF_8)
        .defaultAdvisors(
            new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE),
            MessageChatMemoryAdvisor.builder(chatMemory()).build(),
            financialGuardrailAdvisor)
        .build();
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
