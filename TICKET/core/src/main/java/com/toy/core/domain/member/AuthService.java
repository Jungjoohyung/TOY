package com.toy.core.domain.member;

import com.toy.common.exception.AuthorizationException;
import com.toy.common.exception.BusinessException;
import com.toy.common.exception.EntityNotFoundException;
import com.toy.core.config.JwtUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    public String login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("가입되지 않은 이메일입니다."));

        if (!member.getPassword().equals(password)) {
            throw new AuthorizationException("비밀번호가 일치하지 않습니다.");
        }

        return jwtUtil.createToken(member.getId(), member.getEmail());
    }

    @Transactional
    public Long signup(String email, String password, String name, MemberRole role) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("이미 가입된 이메일입니다.");
        }

        Member member = Member.builder()
                .email(email)
                .password(password)
                .name(name)
                .role(role != null ? role : MemberRole.USER)
                .build();

        return memberRepository.save(member).getId();
    }
}
