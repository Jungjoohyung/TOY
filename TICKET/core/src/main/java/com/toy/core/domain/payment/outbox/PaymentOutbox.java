package com.toy.core.domain.payment.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Outbox 패턴 엔티티.
 * Payment 저장과 동일 트랜잭션 내에 삽입되어 Kafka 발행 전 이벤트 유실을 방지한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_outbox")
public class PaymentOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long paymentId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON: outboxId, paymentId, userId, reservationId, amount

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime processedAt;

    @Builder
    public PaymentOutbox(Long paymentId, Long userId, Long reservationId, int amount, String payload) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
    }

    public void markProcessed() {
        this.status = OutboxStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
    }

    public void incrementRetry() {
        this.retryCount++;
    }

    /** 저장 후 outboxId를 알게 된 시점에 payload를 확정한다. */
    public void updatePayload(String payload) {
        this.payload = payload;
    }
}
