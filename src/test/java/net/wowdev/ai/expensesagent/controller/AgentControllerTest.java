package net.wowdev.ai.expensesagent.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.wowdev.ai.expensesagent.service.ChatService;
import net.wowdev.ai.expensesagent.service.ChatTurn;
import net.wowdev.ai.expensesagent.service.TokenUsage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {
  @Mock ChatService chatService;
  @InjectMocks AgentController controller;

  @Test
  void combinesSuppliedIdsAndReturnsServiceReply() {
    ChatTurn turn = new ChatTurn("hello", "trace", "reply", TokenUsage.unavailable());
    when(chatService.askAgent("hello", "alice:trip-1")).thenReturn(turn);
    assertEquals(turn, controller.askAgent("hello", "alice", "trip-1"));
    verify(chatService).askAgent("hello", "alice:trip-1");
  }

  @Test
  void usesDefaultIds() {
    ChatTurn turn = new ChatTurn("hello", "trace", "reply", TokenUsage.unavailable());
    when(chatService.askAgent("hello", "user-id:session-id")).thenReturn(turn);
    assertEquals(turn, controller.askAgent("hello", "user-id", "session-id"));
  }
}
