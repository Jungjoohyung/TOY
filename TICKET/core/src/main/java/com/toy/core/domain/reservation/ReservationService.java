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

    @Transactional
    public Long reserve(Long userId, Long seatId) {
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new EntityNotFoundException("좌석을 찾을 수 없습니다."));

        if (seat.isReserved()) {
            throw new BusinessException("이미 예약된 좌석입니다.");
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        member.use(seat.getPrice());

        Reservation reservation = Reservation.builder()
                .userId(userId)
                .seat(seat)
                .build();

        reservation.confirm();
        seat.reserve();

        reservationRepository.save(reservation);

        return reservationRepository.save(reservation).getId();
    }

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

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보가 없습니다."));

        member.charge(reservation.getSeat().getPrice());

        reservation.cancel();
        reservation.getSeat().release();
    }

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
