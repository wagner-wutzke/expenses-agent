package net.wowdev.ai.expensesagent;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class ExpensesAgentApplicationTest {
  @Test void mainDelegatesToSpringApplication() {
    String[] args = {"--server.port=0"};
    try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
      ExpensesAgentApplication.main(args);
      spring.verify(() -> SpringApplication.run(ExpensesAgentApplication.class, args));
    }
  }
}
