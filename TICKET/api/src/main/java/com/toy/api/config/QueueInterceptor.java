package com.toy.api.config;

import com.toy.common.exception.BusinessException;
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
        Long userId = (Long) request.getAttribute("userId");

        if (userId != null && !queueRepository.isAllowed(userId)) {
            log.warn("[새치기 감지] 대기열을 통과하지 않은 유저입니다. ID: {}", userId);
            throw new BusinessException("대기열을 순서대로 통과해야 예약할 수 있습니다.");
        }

        return true;
    }
}
