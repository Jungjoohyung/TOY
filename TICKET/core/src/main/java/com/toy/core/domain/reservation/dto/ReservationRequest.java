package com.toy.core.domain.reservation.dto;

import lombok.AllArgsConstructor; // import 추가
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {
    private Long userId; // 누가
    private Long seatId; // 어느 자리를
}