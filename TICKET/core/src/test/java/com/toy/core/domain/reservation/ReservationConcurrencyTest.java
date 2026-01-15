package com.toy.core.domain.reservation;

import com.toy.core.domain.reservation.dto.ReservationRequest;
import com.toy.core.domain.seat.Seat;
import com.toy.core.domain.seat.SeatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
// (테스트용 DB 설정 강제 주입)
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.datasource.url=jdbc:mysql://localhost:3306/ticket_service?serverTimezone=Asia/Seoul",
    "spring.datasource.username=root",
    "spring.datasource.password=root", // 선임님이 설정한 비밀번호
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
    "spring.jpa.hibernate.ddl-auto=update"
})

class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    @DisplayName("동시성 테스트: 100명이 동시에 1개 좌석을 예매하면 딱 1명만 성공해야 한다.")
    void concurrency_test() throws InterruptedException {
        // Given: 좌석 하나 생성 (테스트용)
        Seat seat = seatRepository.save(Seat.builder()
                .seatNumber("VIP-TEST")
                .price(100000)
                .build());
        Long seatId = seat.getId();

        // 100명의 스레드 준비
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 32개 스레드 풀
        CountDownLatch latch = new CountDownLatch(threadCount); // 100명이 다 끝날 때까지 대기시키는 장치

        AtomicInteger successCount = new AtomicInteger(0); // 성공 횟수
        AtomicInteger failCount = new AtomicInteger(0);    // 실패 횟수

        // When: 100명이 동시에 달려듦
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    ReservationRequest request = new ReservationRequest(1L, seatId); // DTO 생성 (생성자 필요하거나 Setter 사용)
                    reservationService.reserve(request);
                    successCount.incrementAndGet(); // 성공하면 카운트 +1
                } catch (Exception e) {
                    failCount.incrementAndGet();    // 실패(이미 예약됨)하면 카운트 +1
                } finally {
                    latch.countDown(); // 작업 끝
                }
            });
        }

        latch.await(); // 100명 다 끝날 때까지 메인 스레드 대기

        // Then: 결과 검증
        System.out.println("성공한 횟수: " + successCount.get());
        System.out.println("실패한 횟수: " + failCount.get());

        assertThat(successCount.get()).isEqualTo(1); // 오직 1명만 성공!
        assertThat(failCount.get()).isEqualTo(99);   // 99명은 튕겨야 함!
    }
}