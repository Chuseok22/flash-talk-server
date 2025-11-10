package com.chuseok22.flashtalk.chat.infrastructure.event.publisher;

import com.chuseok22.flashtalk.rabbitmq.infrastructure.properties.RoutingProperties;
import com.chuseok22.flashtalk.rabbitmq.infrastructure.properties.TopologyProperties;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatMessagePublisher {

  private final RabbitTemplate rabbitTemplate;
  private final RoutingProperties routingProperties;
  private final TopologyProperties topologyProperties;

  public void publishNew(Object payload) {
    rabbitTemplate.convertAndSend(routingProperties.chatExchange(), "chat.send", payload);
  }

  public void scheduleDelete(String messageId, long ttlMs) {
    Message message = MessageBuilder
      .withBody(messageId.getBytes(StandardCharsets.UTF_8))
      .setExpiration(String.valueOf(ttlMs))
      .build();
    rabbitTemplate.send("", topologyProperties.ttlQueue(), message);
  }

  public void broadcastDelete(Object payload) {
    rabbitTemplate.convertAndSend(topologyProperties.broadcastExchange(), "", payload);
  }
}
