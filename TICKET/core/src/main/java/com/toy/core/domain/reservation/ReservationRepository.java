package com.toy.core.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime date);

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    // [테스트용] 예매 시간을 강제로 과거로 변경
    @Modifying
    @Query("UPDATE Reservation r SET r.createdAt = :date WHERE r.id = :id")
    void updateCreatedAt(@Param("id") Long id, @Param("date") LocalDateTime date);

    /**
     * PENDING 만료 예약의 좌석 일괄 릴리즈 (Bulk UPDATE).
     * 엔티티 로드 없이 DB 레벨에서 단일 쿼리로 처리.
     * clearAutomatically: 업데이트 후 1차 캐시(영속성 컨텍스트) 초기화.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Seat s SET s.isReserved = false WHERE s.id IN " +
           "(SELECT r.seat.id FROM Reservation r WHERE r.status = 'PENDING' AND r.createdAt < :cutOff)")
    void releaseSeatsByExpiredPending(@Param("cutOff") LocalDateTime cutOff);

    /**
     * PENDING 만료 예약 일괄 취소 (Bulk UPDATE).
     * 반환값: 취소 처리된 건수.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Reservation r SET r.status = 'CANCELLED' WHERE r.status = 'PENDING' AND r.createdAt < :cutOff")
    int bulkCancelExpiredPending(@Param("cutOff") LocalDateTime cutOff);
}
