package com.toy.api.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toy.core.domain.payment.outbox.PaymentOutboxService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer: payment.completed 토픽 수신.
 * 사이드 이펙트(PG 통신 시뮬, 알림 발송 시뮬)를 메인 결제 트랜잭션과 완전히 분리하여 처리한다.
 *
 * 실제 운영 시:
 *  - simulatePGConfirmation() → 카카오페이/토스 승인 API 호출
 *  - simulateNotification()   → 이메일/슬랙/FCM 알림 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentOutboxService outboxService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "payment.completed",
            groupId = "payment-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record) {
        String payload = record.value();
        log.info("[Kafka 수신] topic={}, partition={}, offset={}, payload={}",
                record.topic(), record.partition(), record.offset(), payload);

        Long outboxId = null;
        try {
            JsonNode node = objectMapper.readTree(payload);
            outboxId = node.get("outboxId") != null ? node.get("outboxId").asLong() : null;
            Long paymentId = node.get("paymentId").asLong();
            Long userId = node.get("userId").asLong();
            int amount = node.get("amount").asInt();

            // 1. PG사 통신 시뮬레이션
            simulatePGConfirmation(paymentId, amount);

            // 2. 알림 발송 시뮬레이션
            simulateNotification(userId, paymentId, amount);

            // 3. Outbox 처리 완료 마킹
            if (outboxId != null) {
                outboxService.markProcessed(outboxId);
            }

        } catch (Exception e) {
            log.error("[Kafka Consumer 처리 실패] payload={}", payload, e);
            if (outboxId != null) {
                outboxService.markFailed(outboxId);
            }
        }
    }

    /**
     * PG사 결제 승인 시뮬레이션.
     * 실제 연동 시: 카카오페이 approve API / 토스페이먼츠 confirm API 호출
     */
    private void simulatePGConfirmation(Long paymentId, int amount) {
        log.info("[PG사 통신 시뮬레이션] paymentId={}, amount={}원 — 결제 승인 완료 (시뮬)", paymentId, amount);
    }

    /**
     * 알림 발송 시뮬레이션.
     * 실제 연동 시: 이메일(SES), 슬랙 Webhook, FCM Push 발송
     */
    private void simulateNotification(Long userId, Long paymentId, int amount) {
        log.info("[알림 발송 시뮬레이션] userId={}, paymentId={}, amount={}원 — 결제 완료 알림 발송 (시뮬)",
                userId, paymentId, amount);
    }
}
