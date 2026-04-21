package com.toy.api.config;

import com.toy.core.domain.member.Member;
import com.toy.core.domain.member.MemberRepository;
import com.toy.core.domain.member.MemberRole;
import com.toy.core.domain.performance.*;
import com.toy.core.domain.seat.Seat;
import com.toy.core.domain.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Order(1)
@Component
@RequiredArgsConstructor
public class InitDb implements CommandLineRunner {

    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initPerformancesAndSeats();
        initTestMembers();
    }

    private void initPerformancesAndSeats() {
        if (performanceRepository.count() > 0) {
            System.out.println("✅ [InitDb] 공연 데이터가 이미 존재합니다. 건너뜁니다.");
            return;
        }

        System.out.println("🚀 [InitDb] 공연/좌석 초기 데이터를 생성합니다...");

        // 공연(Concert) 생성 - 아이유 콘서트
        Concert concert = Concert.builder()
                .title("2026 아이유 앵콜 콘서트 - The Golden Hour")
                .artist("아이유")
                .genre("K-POP")
                .build();
        performanceRepository.save(concert);

        // 공연(Sports) 생성 - 손흥민 경기
        Sports sports = Sports.builder()
                .title("토트넘 vs 뮌헨 친선경기")
                .homeTeam("토트넘")
                .awayTeam("뮌헨")
                .build();
        performanceRepository.save(sports);

        // 스케줄 생성 (예매 어제 오픈, 공연 1시간 전 마감)
        PerformanceSchedule schedule = PerformanceSchedule.builder()
                .performance(concert)
                .startDateTime(LocalDateTime.now().plusDays(7).withHour(19).withMinute(0))
                .bookingStartAt(LocalDateTime.now().minusDays(1).withHour(10).withMinute(0))
                .bookingEndAt(LocalDateTime.now().plusDays(7).withHour(18).withMinute(0))
                .build();
        scheduleRepository.save(schedule);

        // 좌석 생성 (1~500번)
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            int price = (i <= 50) ? 150000 : 110000;
            String seatGrade = (i <= 50) ? "VIP" : "REGULAR";
            seats.add(Seat.builder()
                    .schedule(schedule)
                    .seatNumber(seatGrade + "-" + i)
                    .price(price)
                    .build());
        }
        seatRepository.saveAll(seats);

        System.out.println("🎉 [InitDb] 공연 2개, 스케줄 1개, 좌석 500개 생성 완료!");
    }

    private void initTestMembers() {
        if (memberRepository.count() >= 100) {
            System.out.println("✅ [InitDb] 테스트 유저가 이미 존재합니다. 건너뜁니다.");
            return;
        }

        System.out.println("🚀 [InitDb] 부하 테스트용 유저 100명을 생성합니다...");

        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Member member = Member.builder()
                    .email("test" + i + "@gmail.com")
                    .password("password123")
                    .name("테스터" + i)
                    .role(MemberRole.USER)
                    .build();
            member.charge(10_000_000); // 결제 실패 없도록 1000만 포인트 지급
            members.add(member);
        }
        memberRepository.saveAll(members);

        System.out.println("🎉 [InitDb] 테스트 유저 100명 생성 완료! (test1@gmail.com ~ test100@gmail.com / password123)");
    }
}