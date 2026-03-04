package com.toy.api.controller;

import com.toy.api.facade.ReservationFacade;
import com.toy.common.response.ApiResponse;
import com.toy.core.domain.reservation.dto.ReservationRequest;
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

    private final ReservationFacade reservationFacade;

    @Operation(summary = "좌석 예매", description = "특정 좌석을 선점(예약)합니다.")
    @PostMapping
    public ApiResponse<Long> reserve(@RequestBody ReservationRequest request, HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        Long reservationId = reservationFacade.reserveTicket(userId, request.getSeatId());
        return ApiResponse.ok("예매 성공", reservationId);
    }

    @Operation(summary = "내 예약 목록 조회", description = "로그인한 사용자의 예매 내역을 조회합니다.")
    @GetMapping
    public ApiResponse<List<ReservationResponse>> getMyReservations(HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        return ApiResponse.ok(reservationFacade.getHistory(userId));
    }

    @Operation(summary = "예매 취소", description = "예매를 취소하고 결제 금액을 환불받습니다.")
    @DeleteMapping("/{reservationId}")
    public ApiResponse<Void> cancel(@PathVariable Long reservationId, HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        reservationFacade.cancelTicket(userId, reservationId);
        return ApiResponse.ok("취소 완료. 환불 처리되었습니다.");
    }
}
