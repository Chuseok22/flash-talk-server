package com.chuseok22.flashtalk.chat.infrastructure.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
@Slf4j
@RequiredArgsConstructor
public class BroadcastListener {

  private final Sinks.Many<String> sink;

  @RabbitListener(queues = "#{broadcastEphemeralQueue.name}")
  public void onBroadcast(String json) {
    sink.tryEmitNext(json);
  }

}
