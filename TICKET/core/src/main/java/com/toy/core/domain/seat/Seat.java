package com.toy.core.domain.seat;

import com.toy.core.domain.performance.PerformanceSchedule; // 👈 이거 import 필수!
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "seat")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String seatNumber;

    // [핵심 변경] Performance는 삭제하고 Schedule과 연결합니다.
    // 이유: 좌석은 "공연 전체"가 아니라 "특정 회차"에 종속
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private PerformanceSchedule schedule;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private boolean isReserved;

    // [변경 Performance -> PerformanceSchedule]
    @Builder
    public Seat(String seatNumber, int price, PerformanceSchedule schedule) {
        this.seatNumber = seatNumber;
        this.price = price;
        this.schedule = schedule; // 스케줄 정보 저장
        this.isReserved = false;
    }

    // --- 비즈니스 로직 ---

    public void reserve() {
        if (this.isReserved) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }
        this.isReserved = true;
    }

    public void release() {
        this.isReserved = false;
    }
}