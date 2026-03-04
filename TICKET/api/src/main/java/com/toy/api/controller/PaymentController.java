package com.toy.api.controller;

import com.toy.common.response.ApiResponse;
import com.toy.core.domain.payment.PaymentService;
import com.toy.core.domain.payment.dto.PaymentRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ApiResponse<Long> pay(@RequestBody PaymentRequest request) {
        Long paymentId = paymentService.pay(
                request.getUserId(),
                request.getReservationId(),
                request.getAmount()
        );
        return ApiResponse.ok("결제 성공", paymentId);
    }
}
