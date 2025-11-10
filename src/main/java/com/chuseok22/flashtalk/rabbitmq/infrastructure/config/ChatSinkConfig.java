package com.chuseok22.flashtalk.rabbitmq.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class ChatSinkConfig {

  @Bean
  public Sinks.Many<String> broadcastSink() {
    return Sinks.many().multicast().onBackpressureBuffer();
  }
}
