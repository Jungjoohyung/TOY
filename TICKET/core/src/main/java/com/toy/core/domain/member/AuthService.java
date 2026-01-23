package com.toy.core.domain.member;

import com.toy.core.domain.member.Member;
import com.toy.core.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.toy.core.config.JwtUtil;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    
    public String login(String email, String password) {
        // 1. 회원 찾기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 2. 비밀번호 확인
        if (!member.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 토큰 발급! (ID 대신 토큰을 줌)
        return jwtUtil.createToken(member.getId(), member.getEmail());
    }

    @Transactional
    public Long signup(String email, String password, String name, MemberRole role) {
        // 1. 이메일 중복 검사 (선택 사항)
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 2. 회원 저장
        Member member = Member.builder()
                .email(email)
                .password(password)
                .name(name)
                .role(role != null ? role : MemberRole.USER) // 역할 없으면 USER로
                .build();

        return memberRepository.save(member).getId();
    }
}