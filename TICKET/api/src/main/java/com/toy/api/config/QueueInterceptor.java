package com.toy.api.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.toy.common.exception.BusinessException;
import com.toy.core.domain.queue.QueueRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 대기열 통과 여부 검증 인터셉터.
 *
 * 로컬 캐시(Caffeine) 적용: Redis EXISTS 호출을 5초 캐싱하여 Redis 부하 감소.
 * 트레이드오프: Active 토큰 만료 후 최대 5초간 접근 허용 가능.
 * (Active TTL=5분 대비 5초 오차는 0.08% 수준이며, 좌석 선점은 Redis 분산 락이 별도 보호)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueInterceptor implements HandlerInterceptor {

    private final QueueRepository queueRepository;

    private final Cache<Long, Boolean> localCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .maximumSize(10_000)
            .build();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return true;

        // 긍정(true)만 캐싱: 미통과(false)는 캐싱하지 않아 Active 전환 즉시 반영
        Boolean cached = localCache.getIfPresent(userId);
        boolean allowed;
        if (cached != null) {
            allowed = true; // 캐시에는 true만 저장되어 있음
        } else {
            allowed = queueRepository.isAllowed(userId);
            if (allowed) localCache.put(userId, true);
        }

        if (!allowed) {
            log.warn("[새치기 감지] 대기열을 통과하지 않은 유저입니다. ID: {}", userId);
            throw new BusinessException("대기열을 순서대로 통과해야 예약할 수 있습니다.");
        }

        return true;
    }
}
