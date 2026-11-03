package net.wowdev.ai.expensesagent.controller;

import net.wowdev.ai.expensesagent.service.ChatService;
import net.wowdev.ai.expensesagent.service.ChatTurn;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

  private final ChatService chatService;

  public AgentController(ChatService chatService) {
    this.chatService = chatService;
  }

  @PostMapping("/chat")
  public ChatTurn askAgent(
      @RequestParam String userPrompt,
      @RequestParam(defaultValue = "user-id") String userId,
      @RequestParam(defaultValue = "session-id") String sessionId) {

    String conversationId = userId + ":" + sessionId;
    return chatService.askAgent(userPrompt, conversationId);
  }
}
