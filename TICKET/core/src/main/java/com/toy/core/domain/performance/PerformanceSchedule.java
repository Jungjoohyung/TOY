package com.toy.core.domain.performance;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "performance_schedule")
public class PerformanceSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [학습 포인트 4] 연관관계 매핑 (N:1)
    // 하나의 공연(Performance)에는 여러 개의 회차(Schedule)가 있을 수 있습니다.
    // FetchType.LAZY: 성능 최적화를 위해, 진짜 쓸 때 데이터를 가져옵니다. (지연 로딩)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    /** 예매 오픈 시각 */
    private LocalDateTime bookingStartAt;

    /** 예매 마감 시각 */
    private LocalDateTime bookingEndAt;

    @Builder
    public PerformanceSchedule(Performance performance, LocalDateTime startDateTime,
                               LocalDateTime bookingStartAt, LocalDateTime bookingEndAt) {
        this.performance = performance;
        this.startDateTime = startDateTime;
        this.bookingStartAt = bookingStartAt;
        this.bookingEndAt = bookingEndAt;
    }

    /**
     * 현재 시각 기준으로 예매 상태를 계산한다.
     * bookingStartAt 또는 bookingEndAt 이 null 이면 null 반환.
     */
    public BookingStatus resolveStatus() {
        if (bookingStartAt == null || bookingEndAt == null) return null;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(bookingStartAt)) return BookingStatus.UPCOMING;
        if (now.isAfter(bookingEndAt)) return BookingStatus.CLOSED;
        return BookingStatus.OPEN;
    }
}