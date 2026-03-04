package com.toy.api.controller;

import com.toy.common.exception.AuthorizationException;
import com.toy.common.response.ApiResponse;
import com.toy.core.domain.queue.QueueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@Tag(name = "4. 대기열(Queue) API", description = "트래픽 제어를 위한 대기열 시스템")
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @Operation(summary = "대기열 등록 (줄 서기)", description = "접속 대기열에 등록합니다.")
    @PostMapping
    public ApiResponse<Void> registerQueue(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new AuthorizationException("로그인이 필요한 서비스입니다.");
        }
        queueService.addQueue(userId);
        return ApiResponse.ok("대기열 등록 완료! (순번을 계속 확인해주세요)");
    }

    @Operation(summary = "내 순번 확인 (Polling)", description = "현재 내가 몇 번째인지 확인합니다. (0이면 입장 가능)")
    @GetMapping("/rank")
    public ApiResponse<Long> getRank(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new AuthorizationException("로그인이 필요한 서비스입니다.");
        }
        return ApiResponse.ok(queueService.getOrder(userId));
    }
}
