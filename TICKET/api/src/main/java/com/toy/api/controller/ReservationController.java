package com.toy.api.controller;

import com.toy.core.domain.reservation.ReservationService;
import com.toy.core.domain.reservation.dto.ReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public String reserve(@RequestBody ReservationRequest request) {
        Long reservationId = reservationService.reserve(request);
        return "예매 성공! (예매 번호: " + reservationId + ")";
    }
}