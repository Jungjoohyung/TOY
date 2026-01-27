package com.toy.core.domain.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository queueRepository;

    // 1. 대기열 진입
    public void addQueue(Long userId) {
        Boolean isNew = queueRepository.register(userId);
        if (Boolean.TRUE.equals(isNew)) {
            log.info("사람 추가됨! User ID: {}", userId);
        }
    }

    // 2. 내 대기 순번 조회 
    public Long getOrder(Long userId) {
        Long rank = queueRepository.getRank(userId);
        if (rank == null) {
            // 대기열에 없으면 0 반환 (이미 입장했거나, 줄을 안 선 상태)
            return 0L;
        }
        return rank;
    }
}
