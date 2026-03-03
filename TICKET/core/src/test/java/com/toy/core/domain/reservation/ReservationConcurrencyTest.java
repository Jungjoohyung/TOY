package com.toy.core.domain.reservation;

import com.toy.core.domain.member.Member;
import com.toy.core.domain.member.MemberRepository;
import com.toy.core.domain.member.MemberRole;
import com.toy.core.domain.performance.*; // 👈 Concert, Performance 다 가져오기
import com.toy.core.domain.seat.Seat;
import com.toy.core.domain.seat.SeatRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.LocalDateTime;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReservationConcurrencyTest {

    @Autowired private ReservationService reservationService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private PerformanceRepository performanceRepository;
    @Autowired private PerformanceScheduleRepository performanceScheduleRepository;
    @Autowired private ReservationRepository reservationRepository;

    @Test
    @DisplayName("🔥 100명이 동시에 한 좌석을 예매하면, 딱 1명만 성공해야 한다!")
    void concurrencyTest() throws InterruptedException {
        // 0. 초기화
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        performanceScheduleRepository.deleteAll(); // 순서 중요 (자식 먼저 삭제)
        performanceRepository.deleteAll();
        memberRepository.deleteAll();

        // 1. [준비] 데이터 생성
        
        // 🚨 Performance는 추상 클래스라 생성 불가! -> Concert(자식) 생성
        Performance performance = performanceRepository.save(
            Concert.builder()      // 👈 Performance.builder() 대신 Concert 사용!
                .title("아이유 콘서트")
                .artist("아이유")
                .genre("K-POP")
                .build()
        );
        
        // 스케줄 생성
        PerformanceSchedule schedule = performanceScheduleRepository.save(
            PerformanceSchedule.builder()
                .performance(performance)
                .startDateTime(LocalDateTime.now())
                .build()
        );
        
        // 좌석 생성
        Seat seat = seatRepository.save(
            Seat.builder()
                .schedule(schedule)
                .seatNumber("A-1")
                .price(100000)
                .build()
        );
        Long seatId = seat.getId();

        // 유저 생성
        Member member = memberRepository.save(Member.builder()
                .email("test@concurrent.com")
                .password("1234")
                .name("테스터")
                .role(MemberRole.USER)
                .build());
        
        member.charge(10000000);
        memberRepository.save(member);
        Long userId = member.getId();

        // 2. 동시성 테스트 실행 (기존과 동일)
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    reservationService.reserve(userId, seatId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 3. 검증
        System.out.println("✅ 성공 횟수: " + successCount.get());
        System.out.println("❌ 실패 횟수: " + failCount.get());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(reservationRepository.count()).isEqualTo(1);
    }
}