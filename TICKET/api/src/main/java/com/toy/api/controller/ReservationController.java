package com.toy.api.controller;

import com.toy.core.domain.reservation.ReservationService;
import com.toy.core.domain.reservation.dto.ReservationRequest;
import com.toy.core.domain.reservation.dto.ReservationResponse;

import java.util.List;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    // HttpServletRequest를 파라미터에 추가!
    public String reserve(@RequestBody ReservationRequest request, HttpServletRequest servletRequest) {

        // 1. 인터셉터가 붙여준 꼬리표(userId) 꺼내기
        Long userId = (Long) servletRequest.getAttribute("userId");

        // 2. 예약 진행 (토큰 주인의 ID로 예약!)
        Long reservationId = reservationService.reserve(userId, request.getSeatId());

        return "예매 성공! 예약 ID: " + reservationId;
    }


    @Operation(summary = "내 예약 목록 조회", description = "로그인한 사용자의 예매 내역을 조회합니다.")
    @GetMapping
    public List<ReservationResponse> getMyReservations(HttpServletRequest servletRequest) {
        // 1. 토큰에서 꺼낸 userId (인터셉터가 넣어줌)
        Long userId = (Long) servletRequest.getAttribute("userId");
        
        // 2. 조회 및 반환
        return reservationService.getMyReservations(userId);
    }


    @Operation(summary = "예매 취소", description = "예매를 취소하고 결제 금액을 환불받습니다.")
    @DeleteMapping("/{reservationId}") // DELETE 메서드 사용
    public String cancel(@PathVariable Long reservationId, HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        
        reservationService.cancelReservation(userId, reservationId);
        
        return "취소 완료! 환불 처리되었습니다.";
    }
    
}