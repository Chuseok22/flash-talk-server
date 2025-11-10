package com.chuseok22.flashtalk.rabbitmq.infrastructure.config;

import com.chuseok22.flashtalk.rabbitmq.infrastructure.properties.AmqpProperties;
import com.chuseok22.flashtalk.rabbitmq.infrastructure.properties.RoutingProperties;
import com.chuseok22.flashtalk.rabbitmq.infrastructure.properties.TopologyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({
  AmqpProperties.class,
  RoutingProperties.class,
  TopologyProperties.class
})
public class RabbitMQConfig {

  private final AmqpProperties amqpProperties;
  private final RoutingProperties routingProperties;
  private final TopologyProperties topologyProperties;

  // 브로커 연결 풀 + 퍼블리셔 확인/리턴 활성화
  @Bean
  public CachingConnectionFactory rabbitConnectionFactory() {
    CachingConnectionFactory factory = new CachingConnectionFactory(amqpProperties.host(), amqpProperties.port());
    factory.setUsername(amqpProperties.username());
    factory.setPassword(amqpProperties.password());
    factory.setVirtualHost(amqpProperties.virtualHost());
    factory.setPublisherConfirmType(ConfirmType.CORRELATED);
    factory.setPublisherReturns(true);
    return factory;
  }

  // 앱 기동 시 선언 (Exchange/Queue/Binding) 자동 등록
  @Bean
  public RabbitAdmin rabbitAdmin(CachingConnectionFactory factory) {
    return new RabbitAdmin(factory);
  }

  // Json 직렬화
  @Bean
  public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  // 메시지 발행
  @Bean
  public RabbitTemplate rabbitTemplate(CachingConnectionFactory factory, Jackson2JsonMessageConverter converter) {
    RabbitTemplate template = new RabbitTemplate(factory);
    template.setMessageConverter(converter);
    return template;
  }

  // 리스너 동시성, prefetch
  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
    CachingConnectionFactory connectionFactory,
    Jackson2JsonMessageConverter converter
  ) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(converter);
    factory.setConcurrentConsumers(2);
    factory.setMaxConcurrentConsumers(8);
    factory.setPrefetchCount(50);
    factory.setDefaultRequeueRejected(false);
    return factory;
  }

  // 채팅용 교환기 / 큐 / 바인딩 일괄 선언
  @Bean
  public Declarables chatTopology() {
    TopicExchange exchange = new TopicExchange(routingProperties.chatExchange(), true, false);
    Queue queue = QueueBuilder.durable(routingProperties.chatQueue()).build();

    // 실제 라우팅 키 패턴 사용
    Binding binding = BindingBuilder.bind(queue)
      .to(exchange)
      .with("chat.#");

    return new Declarables(exchange, queue, binding);
  }

  // TTL/DLX Topology (10초 만료 파이프라인)
  @Bean
  public Declarables ttlAndDlxTopology() {
    FanoutExchange broadcast = new FanoutExchange(topologyProperties.broadcastExchange(), true, false);
    DirectExchange expired = new DirectExchange(topologyProperties.expiredExchange(), true, false);

    Queue ttlQueue = QueueBuilder.durable(topologyProperties.ttlQueue())
      .withArgument("x-dead-letter-exchange", topologyProperties.expiredExchange())
      .withArgument("x-dead-letter-routing-key", topologyProperties.expiredRoutingKey())
      .build();

    Queue expiredQueue = QueueBuilder.durable(topologyProperties.expiredQueue()).build();

    Binding expiredBind = BindingBuilder.bind(expiredQueue)
      .to(expired)
      .with(topologyProperties.expiredRoutingKey());

    return new Declarables(broadcast, expired, ttlQueue, expiredQueue, expiredBind);
  }

  @Bean
  public Queue broadcastEphemeralQueue() {
    return new AnonymousQueue();
  }
}
