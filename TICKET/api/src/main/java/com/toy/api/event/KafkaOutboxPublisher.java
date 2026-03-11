package com.toy.api.event;

import com.toy.api.config.KafkaConfig;
import com.toy.core.domain.payment.event.PaymentCompletedEvent;
import com.toy.core.domain.payment.outbox.PaymentOutbox;
import com.toy.core.domain.payment.outbox.PaymentOutboxRepository;
import com.toy.core.domain.payment.outbox.PaymentOutboxService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 결제 완료 이벤트 → Kafka 발행.
 *
 * AFTER_COMMIT: 결제 트랜잭션이 커밋된 후에만 Kafka 발행 시도.
 *   (트랜잭션 롤백 시 이벤트 무시 → Outbox도 함께 롤백되어 재발행 불필요)
 *
 * @Async: HTTP 응답이 Kafka 발행을 기다리지 않아 응답 지연 방지.
 *   발행 실패 시 Outbox는 PENDING 상태로 남아 OutboxRetryScheduler가 재처리.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOutboxPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PaymentOutboxRepository outboxRepository;
    private final PaymentOutboxService outboxService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        Long outboxId = event.getOutboxId();
        Long paymentId = event.getPaymentId();

        PaymentOutbox outbox = outboxRepository.findById(outboxId).orElse(null);
        if (outbox == null) {
            log.error("[Kafka 발행 실패] Outbox 항목 없음. outboxId={}", outboxId);
            return;
        }

        try {
            kafkaTemplate.send(
                    KafkaConfig.TOPIC_PAYMENT_COMPLETED,
                    paymentId.toString(),   // 파티션 키: paymentId
                    outbox.getPayload()
            ).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("[Kafka 발행 실패] outboxId={}, error={}", outboxId, ex.getMessage());
                    // Outbox는 PENDING 유지 → OutboxRetryScheduler가 재처리
                } else {
                    outboxService.markPublished(outboxId);
                    log.info("[Kafka 발행 완료] outboxId={}, topic={}, partition={}, offset={}",
                            outboxId, KafkaConfig.TOPIC_PAYMENT_COMPLETED,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("[Kafka 발행 오류] outboxId={}", outboxId, e);
        }
    }
}
