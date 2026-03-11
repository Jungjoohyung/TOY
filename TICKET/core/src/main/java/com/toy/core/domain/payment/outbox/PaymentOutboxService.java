package com.toy.core.domain.payment.outbox;

import com.toy.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox 상태 변경 전용 서비스.
 * @TransactionalEventListener(AFTER_COMMIT) 이후에는 원래 트랜잭션이 없으므로
 * 각 메서드에 REQUIRES_NEW를 적용해 독립 트랜잭션으로 상태를 업데이트한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutboxService {

    private final PaymentOutboxRepository outboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(Long outboxId) {
        PaymentOutbox outbox = findById(outboxId);
        outbox.markPublished();
        log.info("[Outbox PUBLISHED] outboxId={}", outboxId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessed(Long outboxId) {
        PaymentOutbox outbox = findById(outboxId);
        outbox.markProcessed();
        log.info("[Outbox PROCESSED] outboxId={}", outboxId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long outboxId) {
        PaymentOutbox outbox = findById(outboxId);
        outbox.markFailed();
        log.warn("[Outbox FAILED] outboxId={}, retryCount={}", outboxId, outbox.getRetryCount());
    }

    private PaymentOutbox findById(Long outboxId) {
        return outboxRepository.findById(outboxId)
                .orElseThrow(() -> new EntityNotFoundException("Outbox 항목을 찾을 수 없습니다. id=" + outboxId));
    }
}
