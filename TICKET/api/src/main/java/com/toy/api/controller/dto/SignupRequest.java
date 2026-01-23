package com.toy.api.controller.dto;

import com.toy.core.domain.member.MemberRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {
    private String email;
    private String password;
    private String name;
    private MemberRole role; // USER (일반), ADMIN (관리자)
}