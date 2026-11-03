package net.wowdev.ai.expensesagent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:expenses-agent-context-test;DB_CLOSE_DELAY=-1")
class ExpensesAgentApplicationTests {

  @Autowired ChatMemory chatMemory;

  @Test
  void contextLoads() {}

  @Test
  void usesThirtyMessageDefaultFromApplicationYaml() {
    ArrayList<Message> messages = new ArrayList<>();
    for (int i = 0; i < 31; i++) {
      messages.add(new UserMessage("message-" + i));
    }

    chatMemory.add("default-window-size-test", messages);

    assertEquals(30, chatMemory.get("default-window-size-test").size());
    assertEquals("message-1", chatMemory.get("default-window-size-test").getFirst().getText());
  }
}
