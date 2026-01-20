package com.toy.api.controller;

import com.toy.core.domain.performance.PerformanceService;
import com.toy.core.domain.performance.dto.PerformanceResponse;
import com.toy.core.domain.performance.dto.PerformanceDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "1. 공연(Performance) API", description = "공연 목록 및 상세 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/performances")
public class PerformanceController {

    // [핵심 변경] 리포지토리 대신 '서비스'를 의존합니다.
    private final PerformanceService performanceService;

    @Operation(summary = "공연 목록 조회", description = "등록된 모든 콘서트와 스포츠 경기를 조회합니다.")
    @GetMapping
    public List<PerformanceResponse> getAllPerformances() {
        return performanceService.getAllPerformances();
    }

    @Operation(summary = "공연 상세 조회", description = "공연 ID로 상세 정보와 회차(스케줄) 목록을 조회합니다.")
    @GetMapping("/{id}")
    public PerformanceDetailResponse getPerformance(@PathVariable Long id) { // 👈 반환 타입 변경
        return performanceService.getPerformance(id);
    }
}