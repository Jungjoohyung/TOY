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

// 부모 테이블(Performance)의 'dtype' 컬럼에 저장될 값을 지정합니다.
// 즉, 콘서트 데이터가 들어오면 dtype 컬럼에 "CONCERT"라고 적힙니다.
@DiscriminatorValue("CONCERT")
public class Concert extends Performance {

    private String artist;
    private String genre;

    @Builder
    public Concert(String title, String artist, String genre) {
        super(title); // 부모(Performance)의 생성자를 호출해 title을 세팅합니다.
        this.artist = artist;
        this.genre = genre;
    }
}