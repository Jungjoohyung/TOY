package com.toy.core.domain.queue;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class QueueRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String WAITING_KEY = "waiting_queue";

    @Value("${queue.active-ttl-minutes:5}")
    private long activeTtlMinutes;

    /**
     * 대기열 등록: Redis ZSet에 timestamp를 score로 추가 (O(log N)).
     * 이미 등록된 경우 false 반환.
     */
    public Boolean register(Long userId) {
        long timestamp = System.currentTimeMillis();
        return redisTemplate.opsForZSet().add(WAITING_KEY, userId.toString(), timestamp);
    }

    /**
     * 순번 조회: ZRANK는 O(log N) 연산.
     * ZSet의 정렬 특성상 삽입/조회 모두 효율적.
     */
    public Long getRank(Long userId) {
        Long rank = redisTemplate.opsForZSet().rank(WAITING_KEY, userId.toString());
        return (rank != null) ? rank + 1 : null;
    }

    /**
     * 대기열에서 앞에서 count명 추출 후 제거 (ZRANGE + ZREM).
     * 스케줄러에서 배치 처리 시 사용.
     */
    public Set<Object> popMin(long count) {
        Set<Object> targets = redisTemplate.opsForZSet().range(WAITING_KEY, 0, count - 1);
        if (targets != null && !targets.isEmpty()) {
            redisTemplate.opsForZSet().remove(WAITING_KEY, targets.toArray());
        }
        return targets;
    }

    /**
     * Active 토큰 발급: TTL은 application.yml의 queue.active-ttl-minutes로 조정 가능.
     * SET active:user:{id} true EX {ttl}
     */
    public void activate(Long userId) {
        String key = "active:user:" + userId;
        redisTemplate.opsForValue().set(key, "true", activeTtlMinutes, TimeUnit.MINUTES);
    }

    /**
     * Active 여부 확인: EXISTS 명령 → O(1).
     * QueueInterceptor에서 매 요청마다 호출되나, O(1) Redis 단일 커맨드라 부하 미미.
     * 로컬 캐시 대안은 분산 환경(다중 인스턴스)에서 일관성 보장 불가로 부적합.
     */
    public boolean isAllowed(Long userId) {
        String key = "active:user:" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
