package com.toy.core.domain.performance.dto;

import com.toy.core.domain.performance.Performance;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PerformanceRequest {
    private String name;
    private String place;
    private Integer price;
    private LocalDate startDate;
    private LocalDate endDate;

    // DTO -> Entity 변환 메서드 (편리함!)
    public Performance toEntity() {
        return Performance.builder()
                .name(this.name)
                .place(this.place)
                .price(this.price)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .build();
    }
}