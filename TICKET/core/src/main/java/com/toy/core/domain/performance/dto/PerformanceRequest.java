package com.toy.core.domain.performance.dto;

import com.toy.core.domain.performance.Concert;
import com.toy.core.domain.performance.Performance;
import com.toy.core.domain.performance.Sports;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PerformanceRequest {

    // 1. 공통 필수 정보
    private String type;  // "CONCERT" 또는 "SPORTS"
    private String title; // (구 name)

    // 2. 콘서트일 때 필요한 정보
    private String artist;
    private String genre;

    // 3. 스포츠일 때 필요한 정보
    private String homeTeam;
    private String awayTeam;

    // ⚠️ 주의: price, date는 여기서 안 받습니다! 
    // (구조상 Performance 생성 후 -> Schedule을 따로 등록하는 방식이 맞습니다)

    // DTO -> Entity 변환 (팩토리 메서드 패턴)
    public Performance toEntity() {
        if ("CONCERT".equalsIgnoreCase(this.type)) {
            return Concert.builder()
                    .title(this.title)
                    .artist(this.artist)
                    .genre(this.genre)
                    .build();
        } 
        else if ("SPORTS".equalsIgnoreCase(this.type)) {
            return Sports.builder()
                    .title(this.title)
                    .homeTeam(this.homeTeam)
                    .awayTeam(this.awayTeam)
                    .build();
        } else {
            throw new IllegalArgumentException("잘못된 공연 타입입니다: " + this.type);
        }
    }
}