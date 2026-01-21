package com.toy.core.domain.reservation;

import com.toy.core.domain.reservation.dto.ReservationRequest;
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

    @Transactional // 트랜잭션 필수! (Lock 유지용)
    public Long reserve(Long userId, Long seatId) {
        // 1. 좌석 조회 (비관적 락 적용) - 동시성 제어의 핵심!
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석을 찾을 수 없습니다."));

        // 2. 이미 예약된 좌석인지 확인 (Seat 엔티티의 메서드 활용)
        seat.reserve(); // 상태를 isReserved = true로 변경

        // 3. 예약 정보 저장
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .seat(seat)
                .build();
        
        return reservationRepository.save(reservation).getId();
    }

    @Transactional
    public int cancelExpiredReservations() {
        // 1. 기준 시간 설정 (지금으로부터 5분 전)
        LocalDateTime cutOffTime = LocalDateTime.now().minusMinutes(5);

        // 2. 5분 지난 대기 상태(PENDING) 예매들 조회
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndCreatedAtBefore(
                ReservationStatus.PENDING, 
                cutOffTime
        );

        // 3. 하나씩 취소 처리
        for (Reservation reservation : expiredReservations) {
            reservation.cancel();       // 예매 상태: CANCELLED
            reservation.getSeat().release(); // 좌석 상태: isReserved = false (다시 구매 가능)
        }
        
        return expiredReservations.size(); // 몇 개 지웠는지 반환 (로그용)
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReservationResponse::new) // DTO로 변환
                .toList();
    }
}