package com.toy.api.controller;

import com.toy.api.controller.dto.LoginRequest;
import com.toy.api.controller.dto.SignupRequest;
import com.toy.common.response.ApiResponse;
import com.toy.core.domain.member.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "4. 인증(Auth) API", description = "로그인/회원가입")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 이름으로 가입합니다.")
    @PostMapping("/signup")
    public ApiResponse<Long> signup(@RequestBody @Valid SignupRequest request) {
        Long memberId = authService.signup(
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getRole()
        );
        return ApiResponse.ok("회원가입 성공", memberId);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ApiResponse.ok("로그인 성공", token);
    }
}
