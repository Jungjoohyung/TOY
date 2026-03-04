package com.toy.api.controller;

import com.toy.api.controller.dto.ChargeRequest;
import com.toy.common.response.ApiResponse;
import com.toy.core.domain.member.PointService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@Tag(name = "5. 포인트(Point) API", description = "포인트 충전")
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @Operation(summary = "포인트 충전", description = "내 지갑에 포인트를 충전합니다.")
    @PostMapping("/charge")
    public ApiResponse<Long> charge(@RequestBody ChargeRequest request, HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        long balance = pointService.chargePoint(userId, request.getAmount());
        return ApiResponse.ok("충전 완료", balance);
    }
}
