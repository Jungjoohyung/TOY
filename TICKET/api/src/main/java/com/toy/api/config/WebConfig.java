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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/reservations/**", "/api/points/**") //  예약 API는 검문 필수!
                .excludePathPatterns("/api/auth/**", "/api/performances/**"); // 로그인, 공연 조회는 프리패스
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