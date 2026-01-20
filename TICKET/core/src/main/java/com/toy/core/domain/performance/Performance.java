package com.toy.core.domain.performance;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "performance")

// [학습 포인트 1] 상속 관계 매핑 전략 설정
// JOINED: 부모 테이블과 자식 테이블을 각각 만들고 JOIN으로 데이터를 가져오는 정석적인 방식입니다.
// 장점: 데이터가 정규화되어 저장 공간 효율이 좋고, 확장성이 뛰어납니다.
@Inheritance(strategy = InheritanceType.JOINED)

// [학습 포인트 2] 구분 컬럼 정의
// DB 관점에서 이 row가 '콘서트'인지 '스포츠'인지 구분할 도장(컬럼) 이름을 정합니다.
// 예: performance 테이블에 'dtype'이라는 컬럼이 생깁니다.
@DiscriminatorColumn(name = "dtype")
public abstract class Performance { // 추상 클래스(abstract): 단독으로 객체 생성 불가

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    public Performance(String title) {
        this.title = title;
    }
}