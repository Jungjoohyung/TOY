package com.toy.core.domain.reservation;
// 예매 상태표
public enum ReservationStatus {
    PENDING,   // 예약 대기 (좌석 점유 중)
    PAID,      // 결제 완료 (최종 확정)
    CANCELLED  // 취소됨
}