package com.toy.core.domain.performance.dto;

import com.toy.core.domain.performance.Performance;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class PerformanceResponse {
    private Long id;
    private String name;
    private String place;
    private Integer price;
    private LocalDate startDate;
    private LocalDate endDate;

    // Entity -> DTO 생성자
    public PerformanceResponse(Performance entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.place = entity.getPlace();
        this.price = entity.getPrice();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
    }
}