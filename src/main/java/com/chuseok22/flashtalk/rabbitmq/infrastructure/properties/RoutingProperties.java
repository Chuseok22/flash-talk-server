package com.chuseok22.flashtalk.rabbitmq.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "messaging.routing")
public record RoutingProperties(
  String chatExchange,
  String chatQueue
) {

}
