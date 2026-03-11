package com.toy.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Kafka 설정.
 * - payment.completed 토픽 자동 생성 (파티션 3, 레플리카 1)
 * - @EnableAsync: KafkaOutboxPublisher의 @Async가 별도 스레드에서 실행되도록 활성화
 */
@EnableAsync
@Configuration
public class KafkaConfig {

    public static final String TOPIC_PAYMENT_COMPLETED = "payment.completed";

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(TOPIC_PAYMENT_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
