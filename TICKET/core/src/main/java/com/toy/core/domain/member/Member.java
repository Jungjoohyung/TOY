package com.toy.core.domain.member;

import com.toy.common.exception.BusinessException;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 지갑 잔액
    private long point;

    @Column(nullable = false, unique = true)
    private String email; // 로그인 ID 역할

    @Column(nullable = false)
    private String password; // 암호화해서 저장해야 함 (일단은 평문으로)

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private MemberRole role; // USER, ADMIN

    @Builder
    public Member(String email, String password, String name, MemberRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.point = 0L;
    }

    //충전
    public void charge(long amount) {
        if (amount <= 0) {
            throw new BusinessException("0원 이상만 충전 가능합니다.");
        }
        this.point += amount;
    }

    //결제
    public void use(long amount) {
        if (this.point < amount) {
            throw new BusinessException("잔액이 부족합니다. 포인트를 충전해주세요.");
        }
        this.point -= amount;
    }

}