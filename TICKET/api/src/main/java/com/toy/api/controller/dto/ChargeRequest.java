package com.toy.api.controller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChargeRequest {
    private long amount; // 충전할 금액
}