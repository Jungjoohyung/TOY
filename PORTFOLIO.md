# 🎟️ 대규모 트래픽 처리를 위한 티켓 예매 시스템 (Ticketing System)

## 📌 1. 프로젝트 개요
> **"단기간에 폭증하는 대규모 트래픽 속에서도 데이터 정합성을 보장하고 DB 부하를 방어하는 안정적인 예매 시스템"**

아이유 콘서트, 대형 스포츠 경기 등 수만 명의 사용자가 동시에 접속하여 한정된 좌석을 경쟁적으로 예매하는 상황을 가정하고 설계한 프로젝트입니다. RDBMS가 견딜 수 없는 수준의 부하를 **Redis 기반 대기열**을 통해 1차 방어하고, 동일 좌석에 대한 다중 접근을 **분산 락(Distributed Lock)**으로 제어하여 데이터 정합성(동시성 문제)을 해결하는 데 집중했습니다.

* **개발 기간**: 202X.XX ~ 202X.XX (버전 관리 진행 중)
* **개발 인원**: 1인 (백엔드)
* **주요 특징**: 멀티 모듈 아키텍처, 1만 TPS 부하 대비 최적화, 대기열 시스템, 동시성 제어

---

## 🛠 2. 사용 기술 (Tech Stack)
* **Backend**: Java 17, Spring Boot 3.3.x, Spring Data JPA
* **Database & Cache**: MySQL 8.0, Redis (Redisson/Lettuce), Caffeine Cache
* **Message Broker / Async**: Apache Kafka, Transactional Outbox Pattern
* **Architecture / Config**: Multi-Module (API, Core, Common), Flyway, ShedLock
* **Test**: JUnit5, RestAssured (E2E Integration Test)

---

## 🎯 3. 핵심 설계 및 문제 해결 (Problem Solving)

### ① Redis ZSet을 활용한 대기열 (Queue) 아키텍처 구현
* **문제**: 티켓 오픈 직후 수만 명의 요청이 그대로 DB로 향할 경우 커넥션 풀 고갈 및 서비스 마비.
* **해결**:
  * DB 접근 전, **Redis Sorted Set (ZSet)**에 사용자를 담아 요청을 직렬화하고, `ZRANK`의 O(log N) 성능을 활용해 수만 명의 대기 순번을 실시간으로 프론트엔드에 응답.
  * 1초마다 동작하는 스케줄러(`popMin`)가 서버 수용량(Active Token)만큼만 대기열에서 통과시켜 DB 트래픽을 일정하게 유지.
* **고도화**: 통과된(Active) 유저 식별 시 발생할 수 있는 매 요청 당 Redis 네트워크 I/O 부하를 줄이기 위해, 서버 단에 **Caffeine 로컬 캐시(Positive-only 캐싱 전략)**를 결합. 통과한 유저는 즉시 메모리 캐싱되어 API 응답 속도 극대화. (최대 1만 TPS 처리 대비)

### ② 분산 락(Redisson)을 통한 동시성 제어 및 트랜잭션 튜닝
* **문제**: 수천 명이 "하나의 VIP 좌석"을 동시에 예약하려 할 때 발생하는 동시성 충돌 및 DB 버퍼 락 경합 문제. (초기: 비관적/낙관적 락 등 중복 사용으로 인한 데드락 및 재시도 지옥 발생)
* **해결**: 
  * DB까지 가기 전에 **Redisson 기반 분산 락(`RLock`)**을 적용하여 "1좌석 1스레드" 접근을 보장하도록 병목을 앞단으로 끌어올림.
  * **트랜잭션 바운더리 최적화**: Facade 패턴을 도입, 분산 락을 획득한 **이후**에 DB 트랜잭션(`@Transactional`)이 시작되도록 로직을 분리. 트랜잭션 점유 시간을 최소화하여 DB 커넥션 병목을 근본적으로 제거함.

### ③ O(1) 성능의 Bulk Update로 N+1 문제 해결 (만료 예약 회수)
* **문제**: 결제 제한 시간(5분)이 만료된 예약을 취소하고 좌석을 회수하는 스케줄러 로직이 만료된 티켓 수(N)만큼 Select와 Update를 반복하는 심각한 DB 부하 야기.
* **해결**: 엔티티 단건 업데이트를 버리고 **JPQL Bulk Update**로 쿼리를 분리. <br>
  단 2건의 쿼리(`UPDATE Seat` 와 `UPDATE Reservation`)만으로 데이터 양과 무관하게 즉시 회수 및 취소 처리가 완료되도록 O(1) 수준으로 성능 개선 (`@Modifying(clearAutomatically=true)`로 영속성 컨텍스트 정합성 유지).

### ④ 멀티 인스턴스 환경 고려 및 안정성 강화
* **ShedLock 도입**: 다중 서버(Pod) 환경에서 `QueueScheduler`가 중복 실행되어 대기열 유저를 과다 방출하는 것을 막기 위해 Redis 기반 분산 스케줄러 락 가동.
* **Flyway DB 마이그레이션**: 배포 환경 간 스키마 정합성과 Seed Data 누락 방지를 위해 DB 버전 관리 구축.
* **도메인 책임 분리**: 예약과 결제 도메인을 분리. 예약은 `PENDING` 상태의 "소유권 주장"까지만 담당하고, 실제 포인트 차감과 "최종 확정"은 `PaymentService`에서 결제되도록 책임 분할.

### ⑤ 이벤트 기반 아키텍처(EDA) 및 Outbox 패턴을 통한 트랜잭션 분리
* **문제**: 결제 트랜잭션 내에서 외부 API(PG사, 이메일/카카오톡 알림 등) 호출 시, 외부 서비스 지연이 내부 DB 커넥션 풀 고갈(Cascading Failure)로 이어지거나, 트랜잭션 롤백 시 이벤트 재발송이 불가능한 한계 존재.
* **해결**:
  * **Kafka 비동기 처리**: 결제 완료 시 `payment.completed` 이벤트를 Kafka로 Publish 후 비동기(`@Async`) Consumer가 알림과 외부 통신을 처리하게 하여 결제 트랜잭션을 획기적으로 단축.
  * **Transactional Outbox 패턴 도입**: 메시지 유실(Zero Data Loss) 방지를 위해, DB 트랜잭션과 동일한 생명주기로 `payment_outbox` 테이블에 이벤트를 먼저(PENDING) 기록.
  * DB 커밋이 완료된 후(`@TransactionalEventListener(AFTER_COMMIT)`) Kafka에 발행(`PUBLISHED`). 실패 시 별도의 30초 주기 스케줄러(`OutboxRetryScheduler`)가 PENDING 이벤트를 긁어와 자동 재발행함으로써 서비스 간 궁극적 일관성(Eventual Consistency) 완벽 보장.

### ⑥ 부하 테스트 서버 모니터링 및 병목 튜닝 (Prometheus, Grafana, k6)
* **문제**: 구현된 아키텍처(대기열, 분산 락 등)가 실제 대규모 트래픽에서 병목 없이 예상대로 동작하는지 정량적인 검증 필요.
* **해결 및 튜닝**:
  * **모니터링 인프라 가동**: Spring Boot Actuator와 Micrometer를 연동하여 JVM, 커넥션 풀, HTTP 메트릭을 Prometheus로 수집하고 Grafana 대시보드로 실시간 부하 시각화.
  * **k6 통합 시나리오 테스트**: `로그인 -> 대기열 진입 및 순번 폴링 -> 좌석 예약 -> 결제`로 이어지는 E2E 스크립트를 작성하여 최대 1,000명의 VU(가상 유저) 동시 접속 테스트 진행.
  * **병목 분석 및 개선**: 
    1. **Throughput 지연 해결 (대기열 & 스케줄러 튜닝)**: 1000명 유저 접속 대비 대기열 스케줄러의 `batch-size` 한계 및 ShedLock(다중 서버 동시성 제어 락)의 최소 유지 시간(`lockAtLeastFor`)으로 인해 1회 `iteration` 평균 10초가량 지연되는 병목 식별. 스케줄러의 락 보유 시간을 `10s`에서 `1s`로, 1회 방출량을 `200명`으로 튜닝 결과, iteration 소요 시간이 **10초 -> 3.6초**로 단축되고 시스템 총 처리량(TPS)이 **57회/초 -> 162회/초**로 3배 이상 폭발적으로 향상됨.
    2. **정상 예외 분석(False Alarm 방어)**: 좌석 대비 초과 유입으로 발생한 예약 실패를 5xx 시스템 장애가 아닌, `409 Conflict (이미 선점된 좌석)`을 뱉어내는 완벽한 비즈니스 로직 방어로 해석 및 k6 `expectedStatuses`로 스크립트화 적용. 결과적으로 1,000명 동시 VUs 부하시간 동안 **`http_req_failed: 0.00%`** 에러율 제로와 **`p(95)=114ms`** 의 극강의 응답 속도를 달성하며 서버 생존 완벽 증명.

---

## 📈 4. 프로젝트 주요 성과 및 설계 철학
1. **아키텍처 관점**: 단일 모듈을 넘어 `api`, `core`, `common`으로 헥사고날(Hexagonal) 아키텍처에 근접한 계층 및 관심사 분리. API 컨트롤러와 도메인 서비스가 서로 오염되지 않는 유지보수성 달성.
2. **"사용자 경험" 중심 에러 처리**: `GlobalExceptionHandler`와 추상화된 커스텀 예외(`BusinessException` 등)를 통해, 서버 에러(500)가 아닌 명확한 UX 메시지(ex: "잔액이 부족합니다", "앞에 대기자가 있습니다")가 프론트에 응답되도록 일관된 표준 응답 래퍼(`ApiResponse`) 적용.
3. **E2E 테스트 성공**: RestAssured 및 인메모리(H2) 데이터베이스를 바탕으로, "대기열 진입 → 순번 대기 → 예약 → 결제"까지의 E2E 통합 테스트를 자동화하여 리팩토링 안정성 확보.
