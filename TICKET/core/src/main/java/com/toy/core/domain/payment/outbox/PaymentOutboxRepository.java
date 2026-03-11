package com.toy.core.domain.payment.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutbox, Long> {

    /**
     * 재시도 스케줄러용: 일정 시간이 지나도 PENDING인 항목 조회.
     * INDEX idx_outbox_status_created (status, created_at) 활용.
     */
    List<PaymentOutbox> findByStatusAndCreatedAtBefore(OutboxStatus status, LocalDateTime before);
}
