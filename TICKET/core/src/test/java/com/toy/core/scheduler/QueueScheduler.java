package com.toy.core.scheduler;

import com.toy.core.domain.queue.QueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final QueueRepository queueRepository;

    // 1초(1000ms)마다 실행
    @Scheduled(fixedDelay = 1000)
    public void enterUsers() {
        // 1. 대기열에서 선착순 50명 꺼내기 (입장!)
        long enterCount = 50;
        Set<Object> enteredUsers = queueRepository.popMin(enterCount);

        if (enteredUsers != null && !enteredUsers.isEmpty()) {
            log.info("🚪 입장 성공! {}명 진입: {}", enteredUsers.size(), enteredUsers);
            // (나중엔 여기서 '입장 토큰'을 발급해서 Active Queue로 옮기는 작업을 합니다)
            // 지금은 일단 대기열에서 빼주는 것만으로 '입장 처리'로 간주합니다.
            
            // [추가] 한 명씩 '활성화(Activate)' 시켜주기
            for (Object obj : enteredUsers) {
                Long userId = Long.valueOf(obj.toString());
                queueRepository.activate(userId); // 출입증 발급!
            }

            
        }
    }

    
}