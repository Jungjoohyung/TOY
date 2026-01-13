package com.toy.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// [핵심] 옆 동네(core)도 같이 스캔해라!
@SpringBootApplication(scanBasePackages = "com.toy") 
@EntityScan("com.toy.core") // 엔티티(@Entity) 찾을 위치
@EnableJpaRepositories("com.toy.core") // 레포지토리(Repository) 찾을 위치
public class ToyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToyApplication.class, args);
    }
}