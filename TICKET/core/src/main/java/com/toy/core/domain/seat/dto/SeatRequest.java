package com.toy.core.domain.seat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeatRequest {
    private Long performanceId; // 어느 공연의 좌석인지?
    private String seatNumber;  // "A-1"
    private int price;          // 150000
}