package com.toy.core.domain.reservation;

import com.toy.core.domain.reservation.dto.ReservationRequest;
import com.toy.core.domain.reservation.dto.ReservationResponse;
import com.toy.core.domain.seat.Seat;
import com.toy.core.domain.seat.SeatRepository;
import com.toy.core.domain.member.Member;
import com.toy.core.domain.member.MemberRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final MemberRepository memberRepository;

    @Transactional // 트랜잭션 필수! (Lock 유지용)
    public Long reserve(Long userId, Long seatId) {
        // 1. 좌석 조회 (비관적 락 적용) - 동시성 제어의 핵심!
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석을 찾을 수 없습니다."));

        // 2. 이미 예약된 좌석인지 확인 (Seat 엔티티의 메서드 활용)
        if (seat.isReserved()) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }
        // 2. 회원 조회
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 결제진행 (잔액 부족 시 에러-> 전체 롤백)
        member.use(seat.getPrice());

        // 3. 예약 정보 저장
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .seat(seat)
                .build();

        // 상태 확정 (PENDING -> PAID)
        reservation.confirm();

        // 좌석 점유 표시
        seat.reserve();

        // 저장장
        reservationRepository.save(reservation);

        return reservationRepository.save(reservation).getId();
    }

    //  사용자 요청에 의한 예매 취소 (환불 포함)
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        // 1. 예약 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        // 2. 내 예약이 맞는지 검증
        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
        }

        // 3. 이미 취소된 건지 확인
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }

        // 4. 환불 진행 (회원 조회 -> 돈 돌려주기)
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        // 결제된 금액만큼 다시 충전(Refund)
        member.charge(reservation.getSeat().getPrice());

        // 5. 예약 상태 변경 (CANCELLED) & 좌석 풀기
        reservation.cancel();
        reservation.getSeat().release();
    }

    @Transactional
    public int cancelExpiredReservations() {
        // 1. 기준 시간 설정 (지금으로부터 5분 전)
        LocalDateTime cutOffTime = LocalDateTime.now().minusMinutes(5);

        // 2. 5분 지난 대기 상태(PENDING) 예매들 조회
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndCreatedAtBefore(
                ReservationStatus.PENDING,
                cutOffTime);

        // 3. 하나씩 취소 처리
        for (Reservation reservation : expiredReservations) {
            reservation.cancel(); // 예매 상태: CANCELLED
            reservation.getSeat().release(); // 좌석 상태: isReserved = false (다시 구매 가능)
        }

        return expiredReservations.size(); // 몇 개 지웠는지 반환 (로그용)
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReservationResponse::new) // DTO로 변환
                .toList();
    }
}