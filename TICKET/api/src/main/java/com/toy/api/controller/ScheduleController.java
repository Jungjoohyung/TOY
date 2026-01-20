package com.toy.api.controller;

import com.toy.core.domain.seat.SeatService;
import com.toy.core.domain.seat.dto.SeatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "2. 일정(Schedule) API", description = "회차별 좌석 조회 등")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final SeatService seatService;

    @Operation(summary = "좌석 배치도 조회", description = "특정 회차(Schedule ID)의 좌석 정보를 모두 조회합니다.")
    @GetMapping("/{scheduleId}/seats")
    public List<SeatResponse> getSeats(@PathVariable Long scheduleId) {
        return seatService.getSeatsBySchedule(scheduleId);
    }
}