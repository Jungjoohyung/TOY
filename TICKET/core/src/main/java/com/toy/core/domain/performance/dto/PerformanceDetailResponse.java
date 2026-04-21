package com.toy.core.domain.performance.dto;

import com.toy.core.domain.performance.Performance;
import com.toy.core.domain.performance.PerformanceSchedule;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PerformanceDetailResponse extends PerformanceResponse {
    
    // 회차 목록 추가!
    private List<PerformanceScheduleResponse> schedules;

    public PerformanceDetailResponse(Performance performance, List<PerformanceSchedule> schedules) {
        // 가장 이른 스케줄 기준으로 예매 상태 계산
        super(performance, schedules.stream()
                .filter(s -> s.getBookingStartAt() != null)
                .min(Comparator.comparing(PerformanceSchedule::getBookingStartAt))
                .orElse(null));
        this.schedules = schedules.stream()
                .map(PerformanceScheduleResponse::new)
                .collect(Collectors.toList());
    }
}