package com.toy.core.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 로그인 시 이메일로 회원 찾기
    Optional<Member> findByEmail(String email);
}