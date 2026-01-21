package com.toy.core.domain.reservation.dto;

import com.toy.core.domain.reservation.Reservation;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationResponse {
    private Long id;              // 예약 ID
    private String seatNumber;    // 좌석 번호 (A-1)
    private int price;            // 가격
    private String title;         // 공연 제목 (Schedule -> Performance 타고 가서 가져옴)
    private LocalDateTime reservedAt; // 예약 시간

    public ReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        this.seatNumber = reservation.getSeat().getSeatNumber();
        this.price = reservation.getSeat().getPrice();
        
        // 💡 객체 그래프 탐색: Reservation -> Seat -> Schedule -> Performance
        this.title = reservation.getSeat().getSchedule().getPerformance().getTitle();
        
        this.reservedAt = reservation.getCreatedAt();
    }
}