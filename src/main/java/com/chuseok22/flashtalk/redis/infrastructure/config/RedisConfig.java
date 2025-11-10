package com.chuseok22.flashtalk.redis.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

  @Bean
  public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
    ReactiveRedisConnectionFactory factory
  ) {
    return new ReactiveStringRedisTemplate(factory);
  }
}
