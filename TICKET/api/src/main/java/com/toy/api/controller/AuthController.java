package com.toy.api.controller;

import com.toy.api.controller.dto.LoginRequest;
import com.toy.core.domain.member.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        
        // 원래는 여기서 JWT 토큰을 줘야 하는데, 
        // 일단 데이터가 잘 들어갔는지 확인하기 위해 ID를 문자열로 줍니다.
        return "로그인 성공! 회원 ID: " + token;
    }
}