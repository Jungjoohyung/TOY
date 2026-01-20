package com.toy.core.domain.performance;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// 스포츠 데이터가 들어오면 dtype 컬럼에 "SPORTS"라고 적힙니다.
@DiscriminatorValue("SPORTS")
public class Sports extends Performance {

    private String homeTeam;
    private String awayTeam;

    @Builder
    public Sports(String title, String homeTeam, String awayTeam) {
        super(title);
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }
}