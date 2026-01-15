package com.toy.core.domain.reservation;

import com.toy.core.domain.seat.Seat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // 생성 시간 자동 기록
@Table(name = "reservation")
// 예매 entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // 회원 ID (FK 없이 값만 저장)

    @OneToOne(fetch = FetchType.LAZY) // 좌석 하나당 예매 하나 (1:1)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(nullable = false)
    private int price; // 예매 시점의 가격 (스냅샷)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Reservation(Long userId, Seat seat) {
        this.userId = userId;
        this.seat = seat;
        this.price = seat.getPrice(); // 좌석 가격을 가져와서 박제
        this.status = ReservationStatus.PENDING; // 기본은 대기 상태
    }

    // 예매 취소 편의 메서드
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}