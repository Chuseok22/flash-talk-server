package com.chuseok22.flashtalk.websocket.infrastructure.config;

import com.chuseok22.flashtalk.websocket.infrastructure.properties.WebsocketProperties;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(WebsocketProperties.class)
public class WebsocketConfig {

  private final WebsocketProperties properties;

  @Bean
  public HandlerMapping wsMapping(WebSocketHandler handler) {
    return new SimpleUrlHandlerMapping(Map.of(properties.endpointPath(), handler), -1);
  }
}
