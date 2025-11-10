package com.chuseok22.flashtalk.chat.application.controller;

import com.chuseok22.flashtalk.chat.core.ChatMessage;
import com.chuseok22.flashtalk.chat.infrastructure.repository.ChatMessageRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat/message")
@RequiredArgsConstructor
public class ChatMessageController {

  private final ChatMessageRepository chatMessageRepository;

  @GetMapping("/backlog")
  public Flux<ChatMessage> backlog() {
    long now = Instant.now().getEpochSecond();
    return chatMessageRepository.findActive(now);
  }
}
