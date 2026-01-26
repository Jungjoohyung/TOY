package com.toy.api.controller;

import com.toy.api.facade.ReservationFacade; // 👈 Service 대신 Facade 사용
import com.toy.core.domain.reservation.dto.ReservationRequest; // 👈 기존 DTO 그대로 사용
import com.toy.core.domain.reservation.dto.ReservationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "3. 예약(Reservation) API", description = "좌석 예매 및 결제")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationFacade reservationFacade; // 👈 여기가 핵심 변경점!

    @Operation(summary = "좌석 예매", description = "특정 좌석을 선점(예약)합니다.")
    @PostMapping
    public String reserve(@RequestBody ReservationRequest request, HttpServletRequest servletRequest) {
        // 1. 토큰에서 ID 꺼내기 (기존 로직 유지)
        Long userId = (Long) servletRequest.getAttribute("userId");

        // 2. Facade에게 일 시키기
        Long reservationId = reservationFacade.reserveTicket(userId, request.getSeatId());

        return "예매 성공! 예약 ID: " + reservationId;
    }

    @Operation(summary = "내 예약 목록 조회", description = "로그인한 사용자의 예매 내역을 조회합니다.")
    @GetMapping
    public List<ReservationResponse> getMyReservations(HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        
        // 조회도 Facade 통해 호출
        return reservationFacade.getHistory(userId);
    }

    @Operation(summary = "예매 취소", description = "예매를 취소하고 결제 금액을 환불받습니다.")
    @DeleteMapping("/{reservationId}")
    public String cancel(@PathVariable Long reservationId, HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        
        // 취소도 Facade 통해 호출
        reservationFacade.cancelTicket(userId, reservationId);
        
        return "취소 완료! 환불 처리되었습니다.";
    }
}