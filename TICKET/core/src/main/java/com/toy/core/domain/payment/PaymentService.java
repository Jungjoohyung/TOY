package com.toy.core.domain.payment;

import com.toy.common.exception.BusinessException;
import com.toy.common.exception.EntityNotFoundException;
import com.toy.common.exception.ForbiddenException;
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
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("예매 내역을 찾을 수 없습니다."));

        if (!reservation.getUserId().equals(userId)) {
            throw new ForbiddenException("본인의 예매 내역만 결제할 수 있습니다.");
        }

        if (reservation.getSeat().getPrice() != amount) {
            throw new BusinessException("결제 금액이 올바르지 않습니다.");
        }

        reservation.completePayment();

        Payment payment = Payment.builder()
                .userId(userId)
                .reservation(reservation)
                .amount(amount)
                .build();

        return paymentRepository.save(payment).getId();
    }
}
