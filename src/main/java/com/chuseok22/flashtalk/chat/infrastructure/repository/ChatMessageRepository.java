package com.chuseok22.flashtalk.chat.infrastructure.repository;

import com.chuseok22.flashtalk.chat.core.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepository {

  private final ReactiveStringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.ttl.message-ms}")
  private long messageTtlMs;
  private static final String Z_SET_ACTIVE_KEY = "ft:active";

  public Mono<Void> save(ChatMessage message) {
    try {
      String json = objectMapper.writeValueAsString(message);
      return redisTemplate.opsForValue()
        .set(getMessageKey(message.getId()), json, Duration.ofMillis(messageTtlMs + 2000))
        .then(redisTemplate.opsForZSet().add(Z_SET_ACTIVE_KEY, message.getId(), (double) message.getExpiresAt()))
        .then();
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  public Mono<Void> delete(String id) {
    return redisTemplate.opsForValue().delete(getMessageKey(id))
      .then(redisTemplate.opsForZSet().remove(Z_SET_ACTIVE_KEY, id).then());
  }

  // 만료되지 않은 메시지 로드
  public Flux<ChatMessage> findActive(long nowEpochMs) {
    Range<Double> range = Range
      .from(Range.Bound.inclusive((double) nowEpochMs)) // 하한 포함
      .to(Range.Bound.unbounded()); // 상한 무제한

    return redisTemplate.opsForZSet().rangeByScore(Z_SET_ACTIVE_KEY, range)
      .flatMap(id ->
        redisTemplate.opsForValue().get(getMessageKey(id))
          .flatMap(json ->
            Mono.fromCallable(() -> objectMapper.readValue(json, ChatMessage.class)))
      );
  }

  private String getMessageKey(String id) {
    return "ft:msg" + id;
  }
}
