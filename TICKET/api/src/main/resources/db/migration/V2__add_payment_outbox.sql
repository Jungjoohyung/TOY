-- Payment Outbox 테이블: 결제 완료 이벤트를 Kafka 발행 전까지 안전하게 보관
-- Outbox Pattern: Payment 저장과 동일 트랜잭션으로 삽입되어 이벤트 유실 방지
CREATE TABLE payment_outbox (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    payment_id     BIGINT      NOT NULL,
    user_id        BIGINT      NOT NULL,
    reservation_id BIGINT      NOT NULL,
    amount         INT         NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING / PUBLISHED / PROCESSED / FAILED
    payload        TEXT        NOT NULL,                   -- JSON (outboxId, paymentId, userId, reservationId, amount)
    retry_count    INT         NOT NULL DEFAULT 0,
    created_at     DATETIME(6) NOT NULL,
    processed_at   DATETIME(6),
    CONSTRAINT pk_payment_outbox PRIMARY KEY (id),
    INDEX idx_outbox_status_created (status, created_at)   -- 재시도 스케줄러 조회 최적화
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
