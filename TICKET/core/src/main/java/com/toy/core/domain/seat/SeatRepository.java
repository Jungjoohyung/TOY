package com.toy.core.domain.seat;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    //스케줄 ID로 좌석 찾기 + 좌석 번호 순으로 정렬
    List<Seat> findByScheduleIdOrderBySeatNumberAsc(Long scheduleId);
    // 🔒 비관적 락(쓰기 락)을 걸어서 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :id")
    Optional<Seat> findByIdWithLock(@Param("id") Long id);

    // 🔒 낙관적 락(읽기 락)을 걸어서 조회
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select s from Seat s where s.id = :id")
    Optional<Seat> findByIdWithOptimisticLock(@Param("id") Long id);
}