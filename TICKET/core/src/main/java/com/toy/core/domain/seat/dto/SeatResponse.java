package com.toy.core.domain.seat.dto;

import com.toy.core.domain.seat.Seat;
import lombok.Getter;

@Getter
public class SeatResponse {
    private Long id;
    private String seatNumber; // "A-1"
    private int price;         // 150000
    private boolean isReserved; // true면 회색 처리(선택 불가)

    public SeatResponse(Seat seat) {
        this.id = seat.getId();
        this.seatNumber = seat.getSeatNumber();
        this.price = seat.getPrice();
        this.isReserved = seat.isReserved();
    }
}