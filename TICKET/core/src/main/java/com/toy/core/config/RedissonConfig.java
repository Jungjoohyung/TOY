package com.toy.core.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    private static final String REDIS_HOST_PREFIX = "redis://";

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // docker run으로 띄운 로컬 redis 주소
        config.useSingleServer().setAddress(REDIS_HOST_PREFIX + "127.0.0.1:6379");
        
        return Redisson.create(config);
    }
}