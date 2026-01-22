package com.toy.core.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final MemberRepository memberRepository;

    // 💰 포인트 충전하기
    @Transactional //  DB 상태를 바꾸니까 트랜잭션 필수
    public long chargePoint(Long userId, long amount) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 엔티티에게 충전 명령!
        member.charge(amount);

        // 변경 감지(Dirty Checking) 덕분에 save 안 해도 자동 저장됨
        return member.getPoint();
    }
}