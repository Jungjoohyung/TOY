package com.toy.core.domain.seat;

import com.toy.core.domain.performance.Performance;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "seat") // 테이블 이름 명시
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seatNumber; // 예: "A-1", "B-10"

    @Column(nullable = false)
    private int price; // 좌석별 가격 (VIP석 등 다를 수 있으니)

    @Column(nullable = false)
    private boolean isReserved; // 예약 여부 (true면 이미 팔린 자리)

    // ★ Performance와 연관관계 매핑 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    private Performance performance;

    @Builder
    public Seat(String seatNumber, int price, Performance performance) {
        this.seatNumber = seatNumber;
        this.price = price;
        this.performance = performance;
        this.isReserved = false; // 기본값은 '빈 자리'
    }

    // 예약 처리 메서드 (비즈니스 로직)
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