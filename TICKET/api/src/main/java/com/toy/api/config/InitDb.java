package com.toy.api.config;

import com.toy.core.domain.performance.*;
import com.toy.core.domain.seat.Seat;
import com.toy.core.domain.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InitDb implements CommandLineRunner {

    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. 이미 데이터가 있으면 실행하지 않음 (중복 생성 방지)
        if (performanceRepository.count() > 0) {
            System.out.println("✅ [InitDb] 이미 데이터가 존재하여 초기화를 건너뜁니다.");
            return;
        }

        System.out.println("🚀 [InitDb] 초기 데이터를 생성합니다...");

        // 2. 공연(Concert) 생성 - 아이유 콘서트
        Concert concert = Concert.builder()
                .title("2026 아이유 앵콜 콘서트 - The Golden Hour")
                .artist("아이유")
                .genre("K-POP")
                .build();
        performanceRepository.save(concert);

        // 3. 공연(Sports) 생성 - 손흥민 경기 (옵션)
        Sports sports = Sports.builder()
                .title("토트넘 vs 뮌헨 친선경기")
                .homeTeam("토트넘")
                .awayTeam("뮌헨")
                .build();
        performanceRepository.save(sports);

        // 4. 스케줄 생성 (아이유 콘서트 - 다음주 토요일 저녁 7시)
        PerformanceSchedule schedule = PerformanceSchedule.builder()
                .performance(concert)
                .startDateTime(LocalDateTime.now().plusDays(7).withHour(19).withMinute(0))
                .build();
        scheduleRepository.save(schedule);

        // 5. 좌석 생성 (1~50번 좌석)
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            // 1~10번은 VIP석 (15만원), 나머지는 일반석 (11만원)
            int price = (i <= 10) ? 150000 : 110000;
            String seatGrade = (i <= 10) ? "VIP" : "REGULAR";

            seats.add(Seat.builder()
                    .schedule(schedule)
                    .seatNumber(seatGrade + "-" + i) // 예: VIP-1, REGULAR-11
                    .price(price)
                    .build());
        }
        seatRepository.saveAll(seats); // 한방에 저장

        System.out.println("🎉 [InitDb] 공연 2개, 스케줄 1개, 좌석 50개 생성 완료!");
    }
}