package com.toy.core.domain.payment.event;

import lombok.Getter;

/**
 * 결제 완료 도메인 이벤트.
 * PaymentService.pay() 트랜잭션 커밋 직후 @TransactionalEventListener(AFTER_COMMIT)가 수신한다.
 */
@Getter
public class PaymentCompletedEvent {

    private final Long outboxId;
    private final Long paymentId;

    public PaymentCompletedEvent(Long outboxId, Long paymentId) {
        this.outboxId = outboxId;
        this.paymentId = paymentId;
    }
}
