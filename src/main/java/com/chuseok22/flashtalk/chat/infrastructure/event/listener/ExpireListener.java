package com.chuseok22.flashtalk.chat.infrastructure.event.listener;

import com.chuseok22.flashtalk.chat.infrastructure.event.publisher.ChatMessagePublisher;
import com.chuseok22.flashtalk.chat.infrastructure.repository.ChatMessageRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExpireListener {

  private final ChatMessageRepository chatMessageRepository;
  private final ChatMessagePublisher chatMessagePublisher;

  @RabbitListener(queues = "${messaging.topology.expired-queue}")
  public void onExpired(String messageId) {
    chatMessageRepository.delete(messageId)
      .then(Mono.fromRunnable(() -> {
        Map<String, Object> event = Map.of("type", "message:delete", "id", messageId);
        chatMessagePublisher.broadcastDelete(event);
      }))
      .subscribe();
  }
}
