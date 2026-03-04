package com.toy.api.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * ShedLock 설정.
 * 다중 인스턴스 환경에서 @Scheduled 메서드가 오직 하나의 인스턴스에서만 실행되도록 보장.
 * Redis를 분산 락 스토리지로 사용.
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT50S")
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory);
    }
}
