package com.chuseok22.flashtalk.rabbitmq.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "messaging.topology")
public record TopologyProperties(
  String broadcastExchange,  // ft.broadcast
  String expiredExchange,    // ft.expired
  String ttlQueue,           // ft.ttl
  String expiredQueue,       // ft.expired.q
  String expiredRoutingKey   // expired
) {

}
