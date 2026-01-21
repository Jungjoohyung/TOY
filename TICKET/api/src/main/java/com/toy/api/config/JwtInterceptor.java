package com.toy.api.config;

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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 헤더에서 토큰 꺼내기
        String header = request.getHeader("Authorization");

        // 2. 토큰이 없거나, 이상하면 차단
        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("🚨 [인증 실패] 토큰이 없습니다.");
            throw new IllegalArgumentException("로그인이 필요합니다."); // 401이나 500 에러로 뜸
        }

        // 3. "Bearer " 글자 떼고 순수 토큰만 추출
        String token = header.substring(7);

        // 4. 검증 (위조되거나 만료되면 여기서 에러 펑!)
        Long userId = jwtUtil.getUserId(token);

        // 5. 검증 통과! 요청 객체에 "이 사람은 누구다"라고 꼬리표 붙이기
        request.setAttribute("userId", userId);
        
        log.info("✅ [인증 성공] 사용자 ID: {}", userId);
        return true; // 통과!
    }
}