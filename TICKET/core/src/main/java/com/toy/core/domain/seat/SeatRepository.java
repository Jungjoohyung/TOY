package com.toy.core.domain.seat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    // 특정 공연의 좌석을 다 가져오는 메서드 (나중에 조회할 때 씀)
    List<Seat> findByPerformanceId(Long performanceId);
}