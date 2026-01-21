package com.toy.core.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//예매 저장소
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 필요한 쿼리 메서드는 나중에 추가

    // 🔍 상태가 status이고, 특정 시간(date) 이전에 만들어진 것들 조회
    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime date);

    // 🕒 [테스트용] 예매 시간을 강제로 과거로 돌리는 타임머신 메서드
    @Modifying
    @Query("update Reservation r set r.createdAt = :date where r.id = :id")
    void updateCreatedAt(@Param("id") Long id, @Param("date") LocalDateTime date);
   
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);
    


}