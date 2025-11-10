package com.chuseok22.flashtalk.rabbitmq.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "messaging.broker.amqp")
public record AmqpProperties(
  String host,
  int port,
  String virtualHost,
  String username,
  String password
) {

}
