package com.toy.api.facade;

import com.toy.core.domain.payment.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 결제 유즈케이스 오케스트레이터.
 * 흐름: 예약 확인 → 포인트 잔액 확인 → 포인트 차감 → 결제 완료 처리
 * (원자성이 필요한 전 과정을 PaymentService 단일 트랜잭션으로 위임)
 *
 * 향후 외부 PG 연동, 알림 발송 등 비-DB 로직은 이 Facade에서 트랜잭션 경계 밖에 추가.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;

    public Long processPayment(Long userId, Long reservationId) {
        log.info("[결제 시작] userId={}, reservationId={}", userId, reservationId);
        Long paymentId = paymentService.pay(userId, reservationId);
        log.info("[결제 완료] paymentId={}", paymentId);
        return paymentId;
    }
}
