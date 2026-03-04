package com.toy.api.controller;

import com.toy.api.facade.PaymentFacade;
import com.toy.common.response.ApiResponse;
import com.toy.core.domain.payment.dto.PaymentRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@Tag(name = "6. 결제(Payment) API", description = "포인트로 예매 결제")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @Operation(summary = "결제", description = "PENDING 상태의 예약을 포인트로 결제합니다.")
    @PostMapping
    public ApiResponse<Long> pay(@RequestBody PaymentRequest request, HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        Long paymentId = paymentFacade.processPayment(userId, request.getReservationId());
        return ApiResponse.ok("결제 성공", paymentId);
    }
}
