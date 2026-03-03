package com.toy.api.facade;

import com.toy.core.domain.member.MemberRepository;
import com.toy.core.domain.reservation.ReservationService;
import com.toy.core.domain.reservation.dto.ReservationResponse;
import com.toy.core.domain.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;


import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;
    private final RedissonClient redissonClient;


    

    /**
     * 🎫 예매 진행 (Facade가 조율)
     */
    public Long reserveTicket(Long userId, Long seatId) {
        // 1. 락 이름 정의 (좌석별로 락을 걸어야 함!)
        String lockName = "lock:seat:" + seatId;
        RLock lock = redissonClient.getLock(lockName);

        int maxRetries = 3;
        int currentRetry = 0;

        while (currentRetry < maxRetries) {
            try {
                // 2. 락 획득 시도 (최대 5초 기다리고, 잡으면 3초 뒤에 자동으로 놓음)
                boolean available = lock.tryLock(5, 3, TimeUnit.SECONDS);

                if (!available) {
                    log.warn("⏳ 락 획득 실패 (누가 이미 예매 중): seatId={}", seatId);
                    throw new IllegalStateException("지금 접속자가 많아 예매가 지연되고 있습니다. 다시 시도해주세요.");
                }

                // 3. 락 잡았으니 안전하게 예매 진행! (Service 호출)
                return reservationService.reserve(userId, seatId);

            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                currentRetry++;
                log.warn("낙관적 락 충돌 발생! 재시도 중... (시도 횟수: {}/{}) seatId={}", currentRetry, maxRetries, seatId);
                if (currentRetry >= maxRetries) {
                    throw new IllegalStateException("동시 예매가 너무 많아 실패했습니다. 다시 시도해주세요.", e);
                }
                try {
                    // 잠시 대기 후 재시도
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("예매 중단됨", ie);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("서버 에러가 발생했습니다.", e);
            } finally {
                // 4. 볼일 다 봤으면 락 해제 (중요!)
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        throw new IllegalStateException("예매 처리 중 오류가 발생했습니다.");
    }

    /**
     * ❌ 예매 취소
     */
    @Transactional
    public void cancelTicket(Long userId, Long reservationId) {
        reservationService.cancelReservation(userId, reservationId);
    }

    /**
     * 📜 내역 조회
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getHistory(Long userId) {
        return reservationService.getMyReservations(userId);
    }
}