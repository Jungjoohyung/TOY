package com.toy.core.domain.seat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeatRequest {
    
    // [변경] performanceId -> scheduleId
    private Long scheduleId; 
    
    private String seatNumber; // A-1
    private int price;         // 150000
}