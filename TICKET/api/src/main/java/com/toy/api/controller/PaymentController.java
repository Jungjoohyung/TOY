package com.toy.api.controller;

import com.toy.core.domain.payment.PaymentService;
import com.toy.core.domain.payment.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Long> pay (@RequestBody PaymentRequest request){
        Long paymentId = paymentService.pay(
            request.getUserId(),
            request.getReservationId(),
            request.getAmount()
        );

        return ResponseEntity.ok(paymentId);
    }
}
