package com.toy.core.domain.payment.outbox;

public enum OutboxStatus {
    PENDING,    // 생성됨 — Kafka 발행 전 (재시도 스케줄러 대상)
    PUBLISHED,  // Kafka 발행 완료 — Consumer 처리 대기
    PROCESSED,  // Consumer 처리 완료
    FAILED      // Consumer 처리 실패
}
