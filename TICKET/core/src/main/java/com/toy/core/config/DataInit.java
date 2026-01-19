package com.toy.core.config;

import com.toy.core.domain.seat.Seat;
import com.toy.core.domain.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner{
    private final SeatRepository seatRepository;

    @Override
    public void run(String... args) throws Exception {
        
        System.out.println("🚀 [초기화 시작] DataInit 실행됨!"); 
        // 좌석이 하나도 없으면 50개 생성
        if (seatRepository.count() == 0) {
            for (int i = 1; i <= 50; i++) {
                seatRepository.save(Seat.builder()
                        .seatNumber("A-" + i) // A-1, A-2 ...
                        .price(10000)         // 가격은 만 원
                        .build());
            }
            System.out.println("✅ [데이터 초기화] 좌석 50개가 생성되었습니다!");
        }
    }
    
}
