package com.toy.api.config;

import com.toy.core.domain.queue.QueueRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueInterceptor implements HandlerInterceptor {

    private final QueueRepository queueRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 앞단(JwtInterceptor)에서 넣어준 userId 꺼내기
        Long userId = (Long) request.getAttribute("userId");

        // 2. 대기열 통과했는지 확인
        if (userId != null && !queueRepository.isAllowed(userId)) {
            log.warn("🚨 [새치기 감지] 대기열을 통과하지 않은 유저입니다. ID: {}", userId);
            throw new IllegalStateException("대기열을 순서대로 통과해야 예약할 수 있습니다! (줄 서세요)");
        }

        return true; // 통과!
    }
}