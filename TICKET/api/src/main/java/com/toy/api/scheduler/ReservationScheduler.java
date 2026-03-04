package com.toy.api.scheduler;

import com.toy.core.domain.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    // 1분(60000ms)마다 실행
    @Scheduled(fixedDelay = 60000)
    @SchedulerLock(name = "reservation_auto_cancel_lock", lockAtMostFor = "50s", lockAtLeastFor = "10s")
    public void autoCancel() {
        // 청소 시작
        int cancelledCount = reservationService.cancelExpiredReservations();
        
        // 청소한 게 있을 때만 로그 출력
        if (cancelledCount > 0) {
            log.info("🧹 [자동 취소] 5분 지난 미결제 예매 {}건을 취소했습니다.", cancelledCount);
        }
    }
}