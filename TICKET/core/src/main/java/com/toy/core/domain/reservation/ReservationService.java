package com.toy.core.domain.reservation;

import com.toy.common.exception.BusinessException;
import com.toy.common.exception.EntityNotFoundException;
import com.toy.common.exception.ForbiddenException;
import com.toy.core.domain.member.Member;
import com.toy.core.domain.member.MemberRepository;
import com.toy.core.domain.reservation.dto.ReservationResponse;
import com.toy.core.domain.seat.Seat;
import com.toy.core.domain.seat.SeatRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final MemberRepository memberRepository;

    /**
     * 좌석 선점: 비관적 락으로 좌석을 잠그고 PENDING 예약 생성.
     * 포인트 차감은 결제(PaymentService)에서 수행.
     */
    @Transactional
    public Long reserve(Long userId, Long seatId) {
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new EntityNotFoundException("좌석을 찾을 수 없습니다."));

        if (seat.isReserved()) {
            throw new BusinessException("이미 예약된 좌석입니다.");
        }

        Reservation reservation = Reservation.builder()
                .userId(userId)
                .seat(seat)
                .build();

        seat.reserve();

        return reservationRepository.save(reservation).getId();
    }

    /**
     * 예약 취소: PAID 상태인 경우에만 포인트 환불.
     */
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));

        if (!reservation.getUserId().equals(userId)) {
            throw new ForbiddenException("본인의 예약만 취소할 수 있습니다.");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException("이미 취소된 예약입니다.");
        }

        // 결제가 완료된 경우에만 포인트 환불
        if (reservation.getStatus() == ReservationStatus.PAID) {
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("회원 정보가 없습니다."));
            member.charge(reservation.getPrice());
        }

        reservation.cancel();
        reservation.getSeat().release();
    }

    /**
     * 만료된 PENDING 예약 일괄 취소 (결제 없이 만료된 경우 환불 불필요).
     */
    @Transactional
    public int cancelExpiredReservations() {
        LocalDateTime cutOffTime = LocalDateTime.now().minusMinutes(5);

        List<Reservation> expiredReservations = reservationRepository.findByStatusAndCreatedAtBefore(
                ReservationStatus.PENDING, cutOffTime);

        for (Reservation reservation : expiredReservations) {
            reservation.cancel();
            reservation.getSeat().release();
        }

        return expiredReservations.size();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReservationResponse::new)
                .toList();
    }
}
