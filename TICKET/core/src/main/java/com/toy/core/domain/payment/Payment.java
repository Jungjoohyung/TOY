package com.toy.core.domain.payment;

import com.toy.core.domain.reservation.Reservation;
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
@EntityListeners(AuditingEntityListener.class)
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private int amount; // 결제 금액

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime paidAt; // 결제 일시

    @Builder
    public Payment(Long userId, Reservation reservation, int amount) {
        this.userId = userId;
        this.reservation = reservation;
        this.amount = amount;
    }
}