package com.chuseok22.flashtalk.chat.application.service;

import com.chuseok22.flashtalk.chat.core.ChatMessage;
import com.chuseok22.flashtalk.chat.core.constant.MessageType;
import com.chuseok22.flashtalk.chat.infrastructure.event.publisher.ChatMessagePublisher;
import com.chuseok22.flashtalk.chat.infrastructure.repository.ChatMessageRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

  private final ChatMessageRepository chatMessageRepository;
  private final ChatMessagePublisher chatMessagePublisher;

  @Value("${app.ttl.message-ms}")
  private long ttlMs;

  public Mono<ChatMessage> createText(String nickname, String text) {
    return create(MessageType.TEXT, nickname, text, null, null);
  }

  public Mono<ChatMessage> createImage(String nickname, String mediaId, String dataUrl) {
    return create(MessageType.IMAGE, nickname, null, mediaId, dataUrl);
  }

  private Mono<ChatMessage> create(MessageType messageType, String nickname, String text, String mediaId, String dataUrl) {
    long now = Instant.now().toEpochMilli();
    long exp = now + ttlMs;

    ChatMessage message = ChatMessage.builder()
      .id(UUID.randomUUID().toString())
      .messageType(messageType)
      .text(text)
      .dataUrl(dataUrl)
      .mediaId(mediaId)
      .nickname(nickname)
      .createdAt(now)
      .expiresAt(exp)
      .build();

    return chatMessageRepository.save(message)
      .then(Mono.fromRunnable(() -> {
        chatMessagePublisher.publishNew(message); // 새로운 메시지 발행
        chatMessagePublisher.scheduleDelete(message.getId(), ttlMs); // 10초 삭제 예약
      }))
      .thenReturn(message);
  }
}
