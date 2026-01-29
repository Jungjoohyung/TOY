package com.toy.core.domain.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class QueueRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    // 대기열 키 (은행 번호표 기계 이름)
    private static final String WAITING_KEY = "waiting_queue";

    /**
     * 1. 대기열 등록 (줄 서기)
     * - Redis ZSet에 넣습니다. (Key: 유저ID, Score: 현재시간)
     * - 이미 줄 서 있으면 false 반환
     */
    public Boolean register(Long userId) {
        long timestamp = System.currentTimeMillis();
        // ZADD waiting_queue {timestamp} {userId}
        return redisTemplate.opsForZSet().add(WAITING_KEY, userId.toString(), timestamp);
    }

    /**
     * 2. 내 순서 확인 (몇 등인지?)
     * - 0등부터 시작하므로 +1 해줘야 함
     */
    public Long getRank(Long userId) {
        // ZRANK waiting_queue {userId}
        Long rank = redisTemplate.opsForZSet().rank(WAITING_KEY, userId.toString());
        return (rank != null) ? rank + 1 : null;
    }

    /**
     * 3. 입장 시키기 (대기열에서 제거)
     * - 대기열에서 N명 뽑아서 제거(입장 처리)
     */
    public Set<Object> popMin(long count) {
        // 맨 앞에서부터 count만큼 꺼내옴
        Set<Object> targets = redisTemplate.opsForZSet().range(WAITING_KEY, 0, count - 1);

        if (targets != null && !targets.isEmpty()) {
            // 꺼낸 사람들은 대기열에서 삭제 (이제 은행 창구로 감)
            redisTemplate.opsForZSet().remove(WAITING_KEY, targets.toArray());
        }
        return targets;
    }

    // 5분 동안만 유효한 'active:user:{id}' 키를 만듭니다.
    public void activate(Long userId) {
        String key = "active:user:" + userId;
        redisTemplate.opsForValue().set(key, "true", 5, TimeUnit.MINUTES);
    }

    //  검문 (출입증 있니?)
    public boolean isAllowed(Long userId) {
        String key = "active:user:" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

}
