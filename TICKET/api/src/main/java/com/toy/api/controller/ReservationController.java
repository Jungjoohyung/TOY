package com.toy.api.controller;

import com.toy.core.domain.reservation.ReservationService;
import com.toy.core.domain.reservation.dto.ReservationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "3. 예약(Reservation) API", description = "좌석 예매 및 결제")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "좌석 예매", description = "특정 좌석을 선점(예약)합니다. 동시에 같은 좌석을 요청하면 한 명만 성공합니다.")
    @PostMapping
    public String reserve(@RequestBody ReservationRequest request) {
        Long reservationId = reservationService.reserve(request.getUserId(), request.getSeatId());
        return "예매 성공! 예약 ID: " + reservationId;
    }
}