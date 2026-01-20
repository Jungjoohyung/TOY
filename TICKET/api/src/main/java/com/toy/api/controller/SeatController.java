package com.toy.api.controller;

import com.toy.core.domain.performance.PerformanceSchedule;
import com.toy.core.domain.performance.PerformanceScheduleRepository; // 👈 변경
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
    
    // [변경] 공연 저장소가 아니라 '스케줄 저장소'가 필요함
    private final PerformanceScheduleRepository scheduleRepository;

    @PostMapping
    public String createSeat(@RequestBody SeatRequest request) {
        
        // 1. 스케줄(회차) 정보 찾기 (없으면 에러)
        PerformanceSchedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다."));

        // 2. 좌석 만들기 (Builder 패턴)
        Seat seat = Seat.builder()
                .seatNumber(request.getSeatNumber())
                .price(request.getPrice())
                .schedule(schedule) // 👈 [핵심] performance 대신 schedule 주입
                .build();

        // 3. 저장
        seatRepository.save(seat);

        return "좌석 생성 완료: " + seat.getSeatNumber();
    }
}