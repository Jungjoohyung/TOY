package com.toy.core.domain.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toy.common.exception.BusinessException;
import com.toy.common.exception.EntityNotFoundException;
import com.toy.common.exception.ForbiddenException;
import com.toy.core.domain.member.Member;
import com.toy.core.domain.member.MemberRepository;
import com.toy.core.domain.payment.event.PaymentCompletedEvent;
import com.toy.core.domain.payment.outbox.PaymentOutbox;
import com.toy.core.domain.payment.outbox.PaymentOutboxRepository;
import com.toy.core.domain.reservation.Reservation;
import com.toy.core.domain.reservation.ReservationRepository;
import com.toy.core.domain.reservation.ReservationStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final PaymentOutboxRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * 결제 원자 처리: 예약 확인 → 포인트 차감 → 결제 완료 → Outbox 저장.
     * 모든 DB 작업이 단일 트랜잭션으로 묶여 부분 실패가 없다.
     * 커밋 직후 PaymentCompletedEvent를 발행 → KafkaOutboxPublisher(AFTER_COMMIT)가 수신.
     */
    @Transactional
    public Long pay(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("예매 내역을 찾을 수 없습니다."));

        if (!reservation.getUserId().equals(userId)) {
            throw new ForbiddenException("본인의 예매 내역만 결제할 수 있습니다.");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException("결제 가능한 예약 상태가 아닙니다.");
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        member.use(reservation.getPrice());
        reservation.completePayment();

        Payment payment = Payment.builder()
                .userId(userId)
                .reservation(reservation)
                .amount(reservation.getPrice())
                .build();

        Payment saved = paymentRepository.save(payment);

        // Outbox 저장: Payment 저장과 같은 트랜잭션 → 결제 롤백 시 Outbox도 함께 롤백
        // payload는 저장 후 outboxId를 알게 된 시점에 확정한다 (updatePayload로 더티 체킹 처리)
        PaymentOutbox outbox = PaymentOutbox.builder()
                .paymentId(saved.getId())
                .userId(userId)
                .reservationId(reservationId)
                .amount(reservation.getPrice())
                .payload("{}") // placeholder — 아래에서 outboxId 포함 payload로 교체
                .build();

        PaymentOutbox savedOutbox = outboxRepository.save(outbox);

        // outboxId 확정 후 payload 업데이트 (JPA 더티 체킹으로 커밋 시 반영)
        savedOutbox.updatePayload(buildPayload(savedOutbox.getId(), saved.getId(), userId, reservationId, reservation.getPrice()));

        // 커밋 후 이벤트 발행 (@TransactionalEventListener(AFTER_COMMIT)가 수신)
        eventPublisher.publishEvent(new PaymentCompletedEvent(savedOutbox.getId(), saved.getId()));

        log.info("[결제 트랜잭션 완료] paymentId={}, outboxId={}", saved.getId(), savedOutbox.getId());
        return saved.getId();
    }

    private String buildPayload(Long outboxId, Long paymentId, Long userId, Long reservationId, int amount) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "outboxId", outboxId,
                    "paymentId", paymentId,
                    "userId", userId,
                    "reservationId", reservationId,
                    "amount", amount
            ));
        } catch (JsonProcessingException e) {
            throw new BusinessException("결제 이벤트 페이로드 생성에 실패했습니다.");
        }
    }
}
