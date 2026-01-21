package com.toy.core.config;

import com.toy.core.domain.performance.*;
import com.toy.core.domain.seat.Seat;
import com.toy.core.domain.seat.SeatRepository;
import com.toy.core.domain.member.Member;
import com.toy.core.domain.member.MemberRepository;
import com.toy.core.domain.member.MemberRole;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

        private final PerformanceRepository performanceRepository;
        private final PerformanceScheduleRepository scheduleRepository;
        private final SeatRepository seatRepository;
        private final MemberRepository memberRepository;

        @Override
        public void run(String... args) throws Exception {
                System.out.println("🚀 [초기화 시작] DataInit 실행됨!");

                // 0. 테스트용 회원 생성 (없을 때만)
                if (memberRepository.count() == 0) {
                        Member member = Member.builder()
                                        .email("test@test.com")
                                        .password("1234")
                                        .name("테스트유저")
                                        .role(MemberRole.USER)
                                        .build();
                        memberRepository.save(member);
                        System.out.println("👤 [회원 생성] test@test.com / 1234");
                }

                // 중복 실행 방지 (좌석이 이미 있으면 스킵)
                if (seatRepository.count() > 0) {
                        System.out.println("✅ 이미 데이터가 존재하여 초기화를 건너뜁니다.");
                        return;
                }

                // 1. 공연 생성 (콘서트: 싸이 흠뻑쇼)
                Concert concert = Concert.builder()
                                .title("2026 싸이 흠뻑쇼 - 서울")
                                .artist("PSY")
                                .genre("Dance")
                                .build();
                performanceRepository.save(concert);

                // 2. 경기 생성 (스포츠: 슈퍼매치)
                Sports sports = Sports.builder()
                                .title("K리그 슈퍼매치: FC서울 vs 수원삼성")
                                .homeTeam("FC Seoul")
                                .awayTeam("Suwon Samsung")
                                .build();
                performanceRepository.save(sports);

                // 3. 스케줄(회차) 생성
                // 싸이 공연: 2026년 8월 15일 오후 6시
                PerformanceSchedule concertSchedule = PerformanceSchedule.builder()
                                .performance(concert)
                                .startDateTime(LocalDateTime.of(2026, 8, 15, 18, 0))
                                .build();
                scheduleRepository.save(concertSchedule);

                // 축구 경기: 2026년 8월 16일 오후 7시
                PerformanceSchedule sportsSchedule = PerformanceSchedule.builder()
                                .performance(sports)
                                .startDateTime(LocalDateTime.of(2026, 8, 16, 19, 0))
                                .build();
                scheduleRepository.save(sportsSchedule);

                // 4. 좌석 생성 (각 회차별로 50개씩)
                List<Seat> seats = new ArrayList<>();

                // 4-1. 싸이 콘서트 좌석 (A-1 ~ A-50) -> 가격 15만원
                for (int i = 1; i <= 50; i++) {
                        seats.add(Seat.builder()
                                        .seatNumber("A-" + i)
                                        .price(150000)
                                        .schedule(concertSchedule) // 스케줄 연결!
                                        .build());
                }

                // 4-2. 축구 경기 좌석 (B-1 ~ B-50) -> 가격 5만원
                for (int i = 1; i <= 50; i++) {
                        seats.add(Seat.builder()
                                        .seatNumber("B-" + i)
                                        .price(50000)
                                        .schedule(sportsSchedule) // 스케줄 연결!
                                        .build());
                }

                seatRepository.saveAll(seats);

                System.out.println("✅ [데이터 초기화 완료] 공연 2개, 스케줄 2개, 좌석 100개 생성됨!");
        }
}