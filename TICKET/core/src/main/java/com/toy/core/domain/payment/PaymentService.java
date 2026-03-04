package com.toy.core.domain.payment;

import com.toy.common.exception.BusinessException;
import com.toy.common.exception.EntityNotFoundException;
import com.toy.common.exception.ForbiddenException;
import com.toy.core.domain.member.Member;
import com.toy.core.domain.member.MemberRepository;
import com.toy.core.domain.reservation.Reservation;
import com.toy.core.domain.reservation.ReservationRepository;
import com.toy.core.domain.reservation.ReservationStatus;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    /**
     * 결제 원자 처리: 예약 확인 → 포인트 잔액 확인 → 포인트 차감 → 결제 완료.
     * 단일 트랜잭션으로 묶어 부분 실패 방지.
     */
    @Transactional
    public Long pay(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("예매 내역을 찾을 수 없습니다."));

        if (!reservation.getUserId().equals(userId)) {
            throw new ForbiddenException("본인의 예매 내역만 결제할 수 있습니다.");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException("결제 가능한 예약 상태가 아닙니다.");
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        // 포인트 차감 (잔액 부족 시 Member.use()에서 예외 발생)
        member.use(reservation.getPrice());

        reservation.completePayment();

        Payment payment = Payment.builder()
                .userId(userId)
                .reservation(reservation)
                .amount(reservation.getPrice())
                .build();

        return paymentRepository.save(payment).getId();
    }
}
