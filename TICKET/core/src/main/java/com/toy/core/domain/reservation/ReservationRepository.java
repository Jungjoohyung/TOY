package com.toy.core.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
//예매 저장소
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 필요한 쿼리 메서드는 나중에 추가
}