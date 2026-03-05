package com.toy.api.facade;

import com.toy.common.exception.BusinessException;
import com.toy.core.domain.reservation.ReservationService;
import com.toy.core.domain.reservation.dto.ReservationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 예약 유즈케이스 오케스트레이터.
 * 흐름: (대기열 검증은 QueueInterceptor에서 완료) → Redis 분산 락 획득 → 좌석 선점 → 락 해제
 *
 * 단일 락 체제: Redis 분산 락이 seatId 단위로 요청을 직렬화하므로
 * DB 비관적 락(@Lock) 및 낙관적 락(@Version) 중복 불필요.
 * 트랜잭션은 ReservationService가 소유(@Transactional).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;
    private final RedissonClient redissonClient;

    /**
     * 좌석 예매: Redis 분산 락으로 동시 접근 직렬화 후 좌석 선점.
     * 락 획득 실패 시 즉시 거절 (재시도 없음 — 직렬화된 환경에서 재시도 불필요).
     */
    public Long reserveTicket(Long userId, Long seatId) {
        RLock lock = redissonClient.getLock("lock:seat:" + seatId);
        try {
            if (!lock.tryLock(5, 3, TimeUnit.SECONDS)) {
                log.warn("[락 획득 실패] 현재 다른 사용자가 예매 중: seatId={}", seatId);
                throw new BusinessException("지금 접속자가 많아 예매가 지연되고 있습니다. 다시 시도해주세요.");
            }
            return reservationService.reserve(userId, seatId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("예매 처리 중 서버 오류가 발생했습니다.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void cancelTicket(Long userId, Long reservationId) {
        reservationService.cancelReservation(userId, reservationId);
    }

    public List<ReservationResponse> getHistory(Long userId) {
        return reservationService.getMyReservations(userId);
    }
}
