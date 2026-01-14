package com.toy.api.controller;

import com.toy.core.domain.performance.Performance;
import com.toy.core.domain.performance.PerformanceRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import com.toy.core.domain.performance.dto.PerformanceRequest;
import com.toy.core.domain.performance.dto.PerformanceResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/performances")
public class PerformanceController {

    private final PerformanceRepository performanceRepository;

    @PostMapping
    public PerformanceResponse create(@RequestBody PerformanceRequest request) { // DTO로 받음
        // 1. DTO -> Entity 변환
        Performance performance = request.toEntity();
        
        // 2. 저장
        Performance saved = performanceRepository.save(performance);
        
        // 3. Entity -> DTO 변환 후 반환
        return new PerformanceResponse(saved);
    }
    
    // ▼▼▼ [NEW] 조회 기능 추가 ▼▼▼
    @GetMapping
    public List<PerformanceResponse> findAll() {
        return performanceRepository.findAll().stream()
                .map(PerformanceResponse::new) // Entity -> DTO 변환
                .collect(Collectors.toList());
    }
}