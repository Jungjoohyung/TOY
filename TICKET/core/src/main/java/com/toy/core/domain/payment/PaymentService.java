package com.toy.core.domain.payment;

import com.toy.core.domain.reservation.Reservation;
import com.toy.core.domain.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public Long pay(Long userId, Long reservationId, int amount) {
        // 1. 예매 내역 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예매 내역을 찾을 수 없습니다."));

        // 2. 유효성 검사: 내 예매가 맞나?
        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 예매 내역만 결제할 수 있습니다.");
        }

        // 3. 유효성 검사: 가격이 맞나? (해킹 방지)
        if (reservation.getSeat().getPrice() != amount) {
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
        }

        // 4. 상태 변경 (PENDING -> PAID)
        reservation.completePayment();

        // 5. 결제 정보 저장
        Payment payment = Payment.builder()
                .userId(userId)
                .reservation(reservation)
                .amount(amount)
                .build();

        return paymentRepository.save(payment).getId();
    }
}