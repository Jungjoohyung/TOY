package com.toy.api.controller;

import com.toy.core.domain.performance.Performance;
import com.toy.core.domain.performance.PerformanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/performances")
public class PerformanceController {

    private final PerformanceRepository performanceRepository;

    @PostMapping
    public Performance create(@RequestBody Performance performance) {
        // 원래는 DTO를 써야 하지만, 지금은 테스트를 위해 Entity를 바로 받습니다!
        return performanceRepository.save(performance);
    }
}