package com.toy.core.domain.reservation;

import com.toy.core.domain.seat.Seat;
import com.toy.core.domain.seat.SeatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:mysql://localhost:3306/ticket_service?serverTimezone=Asia/Seoul&characterEncoding=UTF-8",
    "spring.datasource.username=root",
    "spring.datasource.password=root",
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect",
    "spring.jpa.hibernate.ddl-auto=update"
})
class ReservationServiceTest {

    @Autowired private ReservationService reservationService;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private SeatRepository seatRepository;

    @Test
    @DisplayName("5분이 지난 예매는 자동으로 취소되어야 한다")
    @Transactional // 테스트 끝나면 데이터 롤백 (DB 깔끔하게 유지)
    void auto_cancel_test() {
        // 1. 좌석 만들고
        Seat seat = seatRepository.save(Seat.builder()
                .seatNumber("A-1")
                .price(1000)
                .build());

        // 2. 예매 생성 (상태: PENDING, 시간: 지금)
        Reservation reservation = reservationRepository.save(Reservation.builder()
                .userId(1L)
                .seat(seat)
                .build());
        
        // 3. 🕒 타임머신 작동: 예매 시간을 "10분 전"으로 강제 변경
        reservationRepository.updateCreatedAt(reservation.getId(), LocalDateTime.now().minusMinutes(10));

        // 4. 청소기 가동! (Service 메서드 직접 호출)
        int count = reservationService.cancelExpiredReservations();

        // 5. 검증
        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).get();

        System.out.println("삭제된 개수: " + count);
        System.out.println("예매 상태: " + updatedReservation.getStatus());

        assertThat(count).isEqualTo(1); // 1개가 지워져야 함
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED); // 상태는 CANCELLED
        assertThat(updatedReservation.getSeat().isReserved()).isFalse(); // 좌석 락도 풀려야 함
    }
}