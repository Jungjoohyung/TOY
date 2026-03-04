package com.toy.core.domain.member;

import com.toy.common.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final MemberRepository memberRepository;

    @Transactional
    public long chargePoint(Long userId, long amount) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        member.charge(amount);

        return member.getPoint();
    }
}
