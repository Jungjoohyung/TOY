package com.toy.api.controller;

import com.toy.core.domain.performance.Performance;
import com.toy.core.domain.performance.PerformanceRepository;
import com.toy.core.domain.seat.Seat;
import com.toy.core.domain.seat.SeatRepository;
import com.toy.core.domain.seat.dto.SeatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatRepository seatRepository;
    private final PerformanceRepository performanceRepository;

    @PostMapping
    public String createSeat(@RequestBody SeatRequest request) {
        // 1. 공연 정보 찾기 (없으면 에러)
        Performance performance = performanceRepository.findById(request.getPerformanceId())
                .orElseThrow(() -> new IllegalArgumentException("공연을 찾을 수 없습니다."));

        // 2. 좌석 만들기 (Builder 패턴 사용)
        Seat seat = Seat.builder()
                .seatNumber(request.getSeatNumber())
                .price(request.getPrice())
                .performance(performance) // 연관관계 설정
                .build();

        // 3. 저장
        seatRepository.save(seat);

        return "좌석 생성 완료: " + seat.getSeatNumber();
    }
}