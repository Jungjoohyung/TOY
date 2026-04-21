package com.toy.core.domain.performance.dto;

import com.toy.core.domain.performance.BookingStatus;
import com.toy.core.domain.performance.Concert;
import com.toy.core.domain.performance.Performance;
import com.toy.core.domain.performance.PerformanceSchedule;
import com.toy.core.domain.performance.Sports;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
public class PerformanceResponse implements Serializable {

    private Long id;
    private String type;
    private String title;
    private String description;

    /** 예매 상태 (null: 스케줄 미등록) */
    private BookingStatus bookingStatus;
    private LocalDateTime bookingStartAt;
    private LocalDateTime bookingEndAt;

    /** 목록 조회용: 스케줄 없이 기본 정보만 */
    public PerformanceResponse(Performance entity) {
        this(entity, null);
    }

    /** 예매 상태 포함 생성자. schedule 이 null 이면 bookingStatus 는 null */
    public PerformanceResponse(Performance entity, PerformanceSchedule schedule) {
        this.id = entity.getId();
        this.title = entity.getTitle();

        if (entity instanceof Concert) {
            Concert c = (Concert) entity;
            this.type = "CONCERT";
            this.description = c.getArtist() + " (" + c.getGenre() + ")";
        } else if (entity instanceof Sports) {
            Sports s = (Sports) entity;
            this.type = "SPORTS";
            this.description = s.getHomeTeam() + " vs " + s.getAwayTeam();
        } else {
            this.type = "UNKNOWN";
        }

        if (schedule != null) {
            this.bookingStatus = schedule.resolveStatus();
            this.bookingStartAt = schedule.getBookingStartAt();
            this.bookingEndAt = schedule.getBookingEndAt();
        }
    }
}