package com.toy.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:mysql://localhost:3306/ticket_service?serverTimezone=Asia/Seoul&characterEncoding=UTF-8",
    "spring.datasource.username=root",
    "spring.datasource.password=root",  // 👈 본인 DB 비밀번호로 변경하세요!
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver"
})
class RedisTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void redisConnectionTest() {
        // given
        String key = "testKey";
        String value = "hello redis";

        // when
        redisTemplate.opsForValue().set(key, value);
        Object result = redisTemplate.opsForValue().get(key);

        // then
        System.out.println("✅ Redis에서 가져온 값: " + result);
        assertThat(result).isEqualTo(value);
        
        // 청소
        redisTemplate.delete(key);
    }
}