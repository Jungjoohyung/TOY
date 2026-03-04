package com.toy.api.config;

import com.toy.common.exception.AuthorizationException;
import com.toy.core.config.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new AuthorizationException("로그인이 필요합니다.");
        }

        String token = header.substring(7).trim();

        try {
            Long userId = jwtUtil.getUserId(token);
            request.setAttribute("userId", userId);
            log.info("[인증 성공] 사용자 ID: {}", userId);
            return true;
        } catch (Exception e) {
            throw new AuthorizationException("유효하지 않은 토큰입니다.");
        }
    }
}
