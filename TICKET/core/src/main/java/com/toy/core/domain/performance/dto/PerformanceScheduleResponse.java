package com.toy.core.domain.performance.dto;

import com.toy.core.domain.performance.PerformanceSchedule;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PerformanceScheduleResponse {
    private Long id;
    private LocalDateTime startDateTime;

    public PerformanceScheduleResponse(PerformanceSchedule entity) {
        this.id = entity.getId();
        this.startDateTime = entity.getStartDateTime();
    }
}