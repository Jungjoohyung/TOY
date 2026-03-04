package com.toy.api.facade;

import com.toy.common.exception.BusinessException;
import com.toy.core.domain.reservation.ReservationService;
import com.toy.core.domain.reservation.dto.ReservationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;
    private final RedissonClient redissonClient;

    public Long reserveTicket(Long userId, Long seatId) {
        String lockName = "lock:seat:" + seatId;
        RLock lock = redissonClient.getLock(lockName);

        int maxRetries = 3;
        int currentRetry = 0;

        while (currentRetry < maxRetries) {
            try {
                boolean available = lock.tryLock(5, 3, TimeUnit.SECONDS);

                if (!available) {
                    log.warn("[락 획득 실패] 현재 다른 사용자가 예매 중: seatId={}", seatId);
                    throw new BusinessException("지금 접속자가 많아 예매가 지연되고 있습니다. 다시 시도해주세요.");
                }

                return reservationService.reserve(userId, seatId);

            } catch (ObjectOptimisticLockingFailureException e) {
                currentRetry++;
                log.warn("[낙관적 락 충돌] 재시도 중... ({}/{}) seatId={}", currentRetry, maxRetries, seatId);
                if (currentRetry >= maxRetries) {
                    throw new BusinessException("동시 예매가 너무 많아 실패했습니다. 다시 시도해주세요.");
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException("예매 처리 중 서버 오류가 발생했습니다.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("예매 처리 중 서버 오류가 발생했습니다.");
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        throw new BusinessException("예매 처리 중 오류가 발생했습니다.");
    }

    @Transactional
    public void cancelTicket(Long userId, Long reservationId) {
        reservationService.cancelReservation(userId, reservationId);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getHistory(Long userId) {
        return reservationService.getMyReservations(userId);
    }
}
