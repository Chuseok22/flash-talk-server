package com.chuseok22.flashtalk.websocket.infrastructure.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "messaging.websocket")
public record WebsocketProperties(
  String endpointPath,
  List<String> allowedOrigins
) {

}
