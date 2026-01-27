package com.toy.api.controller;

import com.toy.core.domain.queue.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "4. 대기열(Queue) API", description = "트래픽 제어를 위한 대기열 시스템")
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @Operation(summary = "대기열 등록 (줄 서기)", description = "접속 대기열에 등록합니다.")
    @PostMapping
    public String registerQueue(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // 👇 [방어 코드 추가] ID가 없으면 에러 던지기!
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요한 서비스입니다. (헤더에 토큰을 넣어주세요)");
        }
        queueService.addQueue(userId);
        return "대기열 등록 완료! (순번을 계속 확인해주세요)";
    }

    @Operation(summary = "내 순번 확인 (Polling)", description = "현재 내가 몇 번째인지 확인합니다. (0이면 입장 가능)")
    @GetMapping("/rank")
    public Long getRank(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // 👇 [방어 코드 추가]
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요한 서비스입니다.");
        }
        return queueService.getOrder(userId);
    }
}