package com.toy.api.controller;

import com.toy.common.response.ApiResponse;
import com.toy.core.domain.seat.SeatService;
import com.toy.core.domain.seat.dto.SeatRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;

    @PostMapping
    public ApiResponse<String> createSeat(@RequestBody SeatRequest request) {
        String seatNumber = seatService.createSeat(
                request.getScheduleId(),
                request.getSeatNumber(),
                request.getPrice()
        );
        return ApiResponse.ok("좌석 생성 완료", seatNumber);
    }
}
