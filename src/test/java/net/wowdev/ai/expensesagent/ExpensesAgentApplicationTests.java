package net.wowdev.ai.expensesagent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:expenses-agent-context-test;DB_CLOSE_DELAY=-1")
class ExpensesAgentApplicationTests {

  @Test
  void contextLoads() {}
}
