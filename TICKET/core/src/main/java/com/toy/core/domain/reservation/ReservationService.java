package com.toy.core.domain.reservation;

import com.toy.core.domain.reservation.dto.ReservationRequest;
import com.toy.core.domain.seat.Seat;
import com.toy.core.domain.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public Long reserve(ReservationRequest request) {
        // 1. 좌석 조회 (없으면 에러)
        // [변경] 락을 걸면서 조회! (다른 트랜잭션은 여기서 대기 상태가 됨)
        Seat seat = seatRepository.findByIdWithLock(request.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("좌석을 찾을 수 없습니다."));

        // 2. 좌석 점유 (이미 예약됐으면 여기서 에러 터짐 -> Seat.java의 reserve() 호출)
        seat.reserve();

        // 3. 예매 생성
        Reservation reservation = Reservation.builder()
                .userId(request.getUserId())
                .seat(seat)
                .build();

        // 4. 저장 및 ID 반환
        return reservationRepository.save(reservation).getId();
    }
}