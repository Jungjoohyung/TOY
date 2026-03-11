package com.toy.api.scheduler;

import com.toy.api.config.KafkaConfig;
import com.toy.core.domain.payment.outbox.OutboxStatus;
import com.toy.core.domain.payment.outbox.PaymentOutbox;
import com.toy.core.domain.payment.outbox.PaymentOutboxRepository;
import com.toy.core.domain.payment.outbox.PaymentOutboxService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox 재시도 스케줄러 (Failsafe).
 * Kafka 발행 단계(@TransactionalEventListener)가 실패하거나 앱이 재시작되어 이벤트가 유실된 경우,
 * 30초 이상 PENDING 상태로 남은 Outbox 항목을 주기적으로 재발행한다.
 *
 * ShedLock으로 멀티 인스턴스 환경에서 중복 실행을 방지한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRetryScheduler {

    private final PaymentOutboxRepository outboxRepository;
    private final PaymentOutboxService outboxService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 30_000) // 30초마다
    @SchedulerLock(name = "outbox_retry_lock", lockAtMostFor = "25s", lockAtLeastFor = "10s")
    public void retryPendingOutboxes() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(30);
        List<PaymentOutbox> pendingList =
                outboxRepository.findByStatusAndCreatedAtBefore(OutboxStatus.PENDING, cutoff);

        if (pendingList.isEmpty()) return;

        log.info("[Outbox 재시도] PENDING 항목 {}건 재발행 시작", pendingList.size());

        for (PaymentOutbox outbox : pendingList) {
            try {
                kafkaTemplate.send(
                        KafkaConfig.TOPIC_PAYMENT_COMPLETED,
                        outbox.getPaymentId().toString(),
                        outbox.getPayload()
                ).whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Outbox 재시도 실패] outboxId={}", outbox.getId(), ex);
                        outboxService.markFailed(outbox.getId());
                    } else {
                        outboxService.markPublished(outbox.getId());
                        log.info("[Outbox 재시도 완료] outboxId={}", outbox.getId());
                    }
                });
            } catch (Exception e) {
                log.error("[Outbox 재시도 오류] outboxId={}", outbox.getId(), e);
                outboxService.markFailed(outbox.getId());
            }
        }
    }
}
