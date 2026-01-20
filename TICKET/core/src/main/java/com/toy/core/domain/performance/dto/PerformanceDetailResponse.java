package com.toy.core.domain.performance.dto;

import com.toy.core.domain.performance.Performance;
import com.toy.core.domain.performance.PerformanceSchedule;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PerformanceDetailResponse extends PerformanceResponse {
    
    // 회차 목록 추가!
    private List<PerformanceScheduleResponse> schedules;

    public PerformanceDetailResponse(Performance performance, List<PerformanceSchedule> schedules) {
        super(performance); // 부모(기본 정보) 채우기
        this.schedules = schedules.stream()
                .map(PerformanceScheduleResponse::new)
                .collect(Collectors.toList());
    }
}