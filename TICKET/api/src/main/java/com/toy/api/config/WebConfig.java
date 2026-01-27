package com.toy.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final QueueInterceptor queueInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 👇 [수정] 여기에 "/api/queue/**"를 추가해주세요!
                .order(1)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/performances/**");
        // 2. 👇 [추가] 대기열 검문소 (예약 API만 지킴)
        registry.addInterceptor(queueInterceptor)
                .order(2) // 👈 순서 2번 (로그인 검사 끝나면 실행)
                .addPathPatterns("/api/reservations/**"); // 예약할 때만 검사!

    }

    // [추가] CORS 설정 (프론트엔드 연동용용)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 API 요청에 대해
                .allowedOrigins("http://localhost:3000", "http://localhost:5173") // 프론트 주소 허용 (React, Vue, Vite 등)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 쿠키/인증 정보 포함 허용
    }
}