package com.toy.api.controller;

import com.toy.common.exception.EntityNotFoundException;
import com.toy.common.response.ApiResponse;
import com.toy.core.domain.performance.PerformanceSchedule;
import com.toy.core.domain.performance.PerformanceScheduleRepository;
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
    private final PerformanceScheduleRepository scheduleRepository;

    @PostMapping
    public ApiResponse<String> createSeat(@RequestBody SeatRequest request) {
        PerformanceSchedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new EntityNotFoundException("스케줄을 찾을 수 없습니다."));

        Seat seat = Seat.builder()
                .seatNumber(request.getSeatNumber())
                .price(request.getPrice())
                .schedule(schedule)
                .build();

        seatRepository.save(seat);

        return ApiResponse.ok("좌석 생성 완료", seat.getSeatNumber());
    }
}
