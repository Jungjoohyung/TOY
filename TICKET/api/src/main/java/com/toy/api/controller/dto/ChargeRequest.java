package com.toy.api.controller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChargeRequest {
    private long amount; // 충전할 금액
}