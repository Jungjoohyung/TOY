package com.toy.core.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // 🔑 비밀키 (원래는 application.yml에 숨겨야 하지만, 편의상 여기에)
    // 32글자 이상이어야 안전합니다!
    private static final String SECRET_KEY = "toy-ticket-service-secret-key-must-be-long-enough";
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // ⏳ 유효기간: 1시간 (1000ms * 60초 * 60분)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    // 1. 토큰 생성 (로그인 성공 시 호출)
    public String createToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // 토큰 주인(ID)
                .claim("email", email)              // 추가 정보(이메일)
                .setIssuedAt(new Date())            // 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 서명 (도장 쾅!)
                .compact();
    }

    // 2. 토큰에서 ID 꺼내기 (검증용)
    public Long getUserId(String token) {
        return Long.parseLong(Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }
}