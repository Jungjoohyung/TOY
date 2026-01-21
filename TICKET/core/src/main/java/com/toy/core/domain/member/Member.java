package com.toy.core.domain.member;

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
    }
}