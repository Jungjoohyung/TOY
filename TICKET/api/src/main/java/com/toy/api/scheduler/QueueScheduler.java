package com.toy.api.scheduler;

import com.toy.core.domain.queue.QueueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 대기열 입장 스케줄러.
 * 1초마다 실행하여 waiting_queue에서 최대 batch-size명을 꺼내 Active 토큰 발급.
 * 처리량(batch-size)은 application.yml의 queue.batch-size로 조정 가능.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final QueueRepository queueRepository;

    @Value("${queue.batch-size:50}")
    private long batchSize;

    @Scheduled(fixedDelay = 1000)
    public void enterUsers() {
        Set<Object> enteredUsers = queueRepository.popMin(batchSize);

        if (enteredUsers != null && !enteredUsers.isEmpty()) {
            for (Object obj : enteredUsers) {
                Long userId = Long.valueOf(obj.toString());
                queueRepository.activate(userId);
            }
            log.info("[대기열 입장] {}명 Active 전환 완료 (최대 처리량: {}명/초)",
                    enteredUsers.size(), batchSize);
        }
    }
}
