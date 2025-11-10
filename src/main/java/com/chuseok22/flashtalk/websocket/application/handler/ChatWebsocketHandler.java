package com.chuseok22.flashtalk.websocket.application.handler;

import com.chuseok22.flashtalk.chat.application.service.ChatService;
import com.chuseok22.flashtalk.chat.application.service.ImageIngestService;
import com.chuseok22.flashtalk.chat.core.constant.MessageType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatWebsocketHandler implements WebSocketHandler {

  private final ChatService chatService;
  private final ImageIngestService imageIngestService;
  private final Sinks.Many<String> sink;
  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> handle(WebSocketSession session) {
    // 입력 처리
    Mono<Void> inbound = session.receive()
      .map(WebSocketMessage::getPayloadAsText)
      .flatMap(txt -> handleInbound(session, txt))
      .onErrorResume(ex -> Mono.empty())
      .then();

    // 출력 (broadcast sink 구독)
    Mono<Void> outbound = session.send(
      sink.asFlux().map(session::textMessage)
    );

    return Mono.when(inbound, outbound);
  }

  private Mono<Void> handleInbound(WebSocketSession session, String json) {
    try {
      JsonNode jsonNode = objectMapper.readTree(json);
      String type = jsonNode.path("type").asText();

      if (type.equals("register")) {
        String nickname = jsonNode.path("nickname").asText();
        if (nickname == null || nickname.isBlank()) {
          nickname = "익명";
        }
        session.getAttributes().put("nickname", nickname);
        return Mono.empty();
      }

      if (type.equals("message")) {
        String nickname = session.getAttributes().get("nickname").toString();
        if (nickname == null || nickname.isBlank()) {
          nickname = "익명";
          session.getAttributes().put("nickname", nickname);
        }

        JsonNode payload = jsonNode.path("payload");
        String messageTypeStr = payload.path("messageType").asText();
        MessageType messageType = MessageType.from(messageTypeStr);

        if (messageType == MessageType.TEXT) {
          String text = payload.path("text").asText();
          if (text != null && !text.isBlank()) {
            if (text.length() > 400) {
              String truncated = text.substring(0, 400);
              return chatService.createText(nickname, truncated).then();
            }
            return chatService.createText(nickname, text).then();
          }
        } else if (messageType == MessageType.IMAGE) {
          String contentType = payload.path("contentType").asText();
          String data = payload.path("dataBase64").asText();
          if (data == null || data.isBlank()) {
            data = payload.path("dataUrl").asText();
          }
          if (contentType != null && !contentType.isBlank() && data != null && !data.isBlank()) {
            String finalNickname = nickname;
            return imageIngestService.ingest(contentType, data)
              .flatMap(response -> chatService.createImage(finalNickname, response.mediaId(), response.dataUrl()))
              .then();
          }
        }
      }
    } catch (Exception ignored) {

    }
    return Mono.empty();
  }
}
