package com.toy.api.controller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
}