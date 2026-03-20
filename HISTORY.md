# 프로젝트 진행 히스토리 및 트러블슈팅 내역

본 문서는 티켓팅 시스템(TOY)을 구축하면서 진행한 주요 과제(Task)별 아키텍처 설계 배경, 구조적 변화, 그리고 해결한 트러블슈팅 내역을 기록합니다.

---

## 🚀 기수별 진행 내역 (Tasks)

### [과제 1] 기반 아키텍처 및 공통 응답 구조 설계
* **목표**: 멀티 모듈 뼈대 구축 및 API 응답 일관성 확보.
* **구현 내용**:
  * **멀티 모듈 구성**: `api` (웹/컨트롤러), `core` (도메인/DB/비즈니스), `common` (유틸/공통 예외) 로 관심사 분리.
  * **전역 예외 처리**: `GlobalExceptionHandler`를 통해 `BusinessException`, `EntityNotFoundException` 등 도메인 예외를 HTTP 4xx 및 5xx 에러로 일관되게 핸들링.
  * **공통 응답 래퍼**: `ApiResponse<T>` 제네릭 클래스를 도입해 모든 API 응답을 `{ "status": ..., "message": ..., "data": ... }` 형태로 통일.

### [과제 2] Facade 계층 고도화 및 트랜잭션 최적화
* **목표**: 예약과 결제 도메인 간의 강결합 문제 해결 및 DB 데드락 방지.
* **설계 결정**:
  * **책임 분리**: 예약(`ReservationService`)은 좌석 선점(PENDING)까지만 처리하고, 결제(`PaymentService`)에서 소유권 검증 및 포인트 차감을 전담하도록 도메인 로직을 분리.
  * **Facade 오케스트레이터**: 컨트롤러가 서비스를 직접 호출하지 않고 `ReservationFacade`, `PaymentFacade`를 통해 여러 도메인(예약+결제 등)의 흐름을 조율.
  * **트랜잭션 바운더리 최적화**: Facade 자체에는 `@Transactional` 속성을 제거하여 트랜잭션 지속 시간을 최소화. **분산 락(Redisson)을 트랜잭션 외부에서 획득**한 뒤 내부 서비스의 `@Transactional` 애노테이션이 달린 메서드를 호출하게 하여, DB 커넥션 풀 고갈과 데드락 위험을 근본적으로 차단.

### [과제 3] 대기열 성능 튜닝 및 만료 예약 일괄 취소 도입
* **목표**: 티켓 오픈 시 대규모 접속자(수만 명) 유입으로부터 DB를 보호하고, 미결제 좌석을 빠르게 회수.
* **설계 결정**:
  * **Redis Sorted Set (ZSet) 대기열**: DB 부하 0을 목표로, 접속 순서를 Timestamp score로 저장(O(log N)). 실시간 순번(`ZRANK`) 및 폴링 처리에 최적화.
  * **Active 토큰 전환 스케줄러**: 1초마다 동작하는 `QueueScheduler`가 설정된 `batch-size`만큼 대기열에서 유저를 꺼내 Active 상태(TTL 5분)로 전환.
  * **만료 예약(PENDING) 일괄 취소 (Bulk Update)**: 결제 제한 시간(5분)이 지난 예약을 기존의 엔티티 별 반복문 처리(N+1 문제)에서 수정하여, **단 2개의 JPQL Bulk Update 쿼리(좌석 isReserved=false, 예약 status=CANCELLED)**로 O(1) 성능 확보. `@Modifying(clearAutomatically = true)`를 통해 영속성 컨텍스트 초기화로 데이터 불일치 방지.

### [과제 4] Flyway 도입 및 ShedLock 다중화 방어 세팅
* **목표**: 분산/다중 서버 환경에서의 스키마 정합성과 스케줄러 중복 실행 방지.
* **구현 내용**:
  * **Flyway DB 마이그레이션**: `V1__init.sql`을 통해 `member`, `performance`(JOINED 상속), `seat`, `reservation` 등의 초기 스키마와 Seed 데이터(아이유 콘서트 등)를 형상 관리.
  * **ShedLock**: 여러 대의 API 서버(Pod)가 뜰 경우 `QueueScheduler`가 중복 실행되어 Active 유저가 과다 방출되는 것을 막기 위해 다중화 제어용 분산 락(`@SchedulerLock`) 적용.

### [과제 5] 이벤트 기반 결제 책임 분리 (Event-Driven Architecture & Outbox Pattern)
* **목표**: 결제 이후 발생할 수 있는 외부 통신(PG사 결제 승인, 알림 발송 등)으로 인한 내부 트랜잭션 지연 및 예외 전파 방지.
* **설계 결정**:
  * **Kafka 비동기 큐 도입**: 기존 `PaymentService` 하나에 묶여있던 로직에서 '결제 성공 알림', '외부 API 전송' 등을 `payment.completed` 이벤트 토픽을 통해 비동기로 Consumer에게 위임.
  * **Transactional Outbox 패턴 (`payment_outbox`)**: 이벤트를 Kafka로 쏘기 전 `payment_outbox` 테이블에 먼저 저장하고 커밋(`@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` 결합)하여, 카프카 브로커 장애 시에도 메시지 유실 제로(Zero Data Loss) 보장.
  * **스케줄러 통한 재발행 보장 (Retry Scheduling)**: `OutboxRetryScheduler`가 주기적으로 PENDING 상태의 이벤트를 읽어 Kafka에 재발행함으로써 궁극적 일관성(Eventual Consistency) 완전 달성.

---

## 🛠️ 주요 트러블슈팅 및 버그 픽스 내역

### 1. `ApiResponse` 오버로딩 제네릭 모호성 충돌
* **증상**: `ApiResponse.ok("서버 ON")` 호출 시, 제네릭 `T`를 `String`으로 추론하지 못하고 `ok(String message)` 콜백(리턴 타입 `Void`)이 우선 매칭되어 컴파일 에러 발생.
* **해결**: 타입 추론의 모호성을 제거하기 위해, 성공 메시지만 담는 빈 데이터 응답 메서드 이름을 `ok(String)`에서 `success(String)`으로 명확히 구분 및 리팩토링.

### 2. 대기열 활성화 후 무한 대기 (순번 -1 버그) 
* **증상**: 대기열을 통과해 `QueueScheduler`가 Active 처리(Redis `hasKey` 생성)를 했음에도 불구하고, 프론트엔드에서 무한 로딩 창이 뜨며 순번이 "-1명 남았습니다"로 표기됨.
* **원인 분석**:
  1. **로컬 캐시(Caffeine) 설계 결함**: `QueueInterceptor`에서 통과 여부(`isAllowed`) 쿼리 결과를 5초간 캐싱하는데, `false`(미통과) 결과마저 5초간 캐싱해버려 Active 전환 직후에도 여전히 차단됨.
  2. **API 계약 구조**: `QueueService.getOrder()`가 대상이 Active이거나 대기열에 아예 없는 경우 모두 `0L`을 반환. (프론트는 null만 기대함)
* **해결**:
  * **Positive-only 캐싱 전략 도입**: `QueueInterceptor`에서 `true`(통과)인 경우에만 `localCache.put`을 수행하도록 튜닝. 차단된 유저는 Redis에 매번 상태를 묻지만(O(1) 해시 코스트라 경미함), 통과 즉시 1초 이내로 대기열 입장이 보장됨.
  * 프론트엔드(`test.html`) 단에서 0도 통과 상태(`isActive`)로 인지하도록 자바스크립트 로직 교정.

### 3. JCache & Redis CacheManager 충돌 (Cannot find cache named 'performances')
* **증상**: `@Cacheable("performances")` 적용 후 앱 구동 시 캐시 매니저를 찾을 수 없다며 런타임 에러 발생.
* **원인**: `redisson-spring-boot-starter` 라이브러리가 내부에 JCache SPI 프로바이더를 갖고 있어, Spring Boot의 Auto Configuration이 기본 설계된 RedisCacheManager 대신 JCache 모듈을 강제 활성화함.
* **해결**: `application.yml`에 `spring.cache.type: redis` 프로퍼티를 명시적으로 선언하여 기본 프로바이더를 Redis로 강제 지정.

### 4. RestAssured E2E 테스트 숫자 (Long/Integer) 캐스팅 에러
* **증상**: `jsonPath().path("data")` 추출 시 200000과 같은 작은 숫자는 내부적으로 `Integer`로 파싱되어, `Long` 타입 변수에 할당하려 할 때 `ClassCastException` 발생.
* **해결**: `.extract().jsonPath().getLong("data")` 명시적 타입 변환 메서드를 사용하여 해결.

### 5. Member 예외의 프론트엔드 노출 누락 (500 에러 치환)
* **증상**: 포인트 충전/사용 시 잔액이 부족하면 "서버 오류"만 표시됨.
* **원인**: 엔티티 클래스 내에서 자바 기본 `IllegalArgumentException`을 던지고 있었고, 이는 `GlobalExceptionHandler`의 최종 `Exception` 래퍼(500 Internal Server Error)에 잡혀 실제 메시지가 감춰짐.
* **해결**: 커스텀 도메인 예외인 `BusinessException`(400 Bad Request)을 던지도록 도메인 객체 내부 코드 수정.

### [과제 6] 부하 테스트 & 모니터링 환경 구축 및 병목 분석
* **목표**: k6 부하 테스트 도구와 Prometheus, Grafana를 활용하여 시스템 병목 구간 식별 및 개선.
* **구현 내용**:
  * **모니터링 인프라**: `docker-compose.yml` 에 Prometheus와 Grafana 볼륨 추가 (자동 프로비저닝된 대시보드 적용).
  * **Spring Actuator 연동**: API `build.gradle.kts` 에 Micrometer 의존성을 추가해 메트릭 수집 개방(`host.docker.internal:8080/actuator/prometheus`).
  * **k6 스크립트 작성**: 로그인 -> 대기열 진입 -> 실시간 랭킹 순번 폴링 -> 좌석 예약 -> 결제로 이어지는 통합 시나리오(`reservation_flow.js`) 구현.
* **트러블슈팅 및 튜닝 내역**:
  * **InitDb 데이터 비일관성 방어 로직 우회**: 기존의 `if (performanceRepository.count() > 0)` 가드가 작동하여 유저 생성이 무시되는 문제를 겪었습니다. 로직을 `initPerformancesAndSeats()`, `initTestMembers()` 두 가지로 분리하여 각기 독립적인 영속화 가드를 가지도록 변경했습니다. 더불어 100명의 유저와 1000만 포인트가 테스트 환경에 무조건 준비되도록 개선했습니다.
  * **QueueScheduler 활성화 병목 해결**: 1000명의 유저(VU)가 접속했을 때 평균 처리 시간(`iteration_duration`)이 10초까지 지연되는 원인이 "1초에 200명씩 활성화시켰지만 ShedLock이 10초간 `lockAtLeastFor`를 점유하여 실제 10초에 한 번만 동작"했기 때문임을 파악했습니다. 스케줄러 락 점유 시간을 `10s -> 1s`로 하향 튜닝한 결과, 평균 소요 시간이 **10.11초 -> 3.6초**로 단축되고 시스템 총 처리량(TPS)이 **57회/초 -> 162회/초**로 3배 이상 폭발적으로 향상되는 성과를 거두었습니다.
  * **비즈니스 트랜잭션 경합 분석**: 병합률 테스트에서 `http_req_failed`가 6.13%(목표치 5% 초과)로 집계되었습니다. 하지만 분석 결과 5xx 장애가 아닌, 동시 다발적 1000명의 VU가 500개 좌석으로 집중되며 예약 경합에 밀린 유저들의 `409 Conflict (이미 선점된 좌석)` 정상 응답이었음을 확인했습니다. 이를 k6 `expectedStatuses(200, 409)` 로직으로 비즈니스 정상 예외 처리함으로써 1,000명의 극심한 부하 속에서도 **`http_req_failed: 0.00%`** 방어율과 **`p(95)=114.75ms`** 응답 속도를 기록하며 서버 생존을 완벽하게 증명했습니다.

---

> 본 문서는 프로젝트 버전업이나 구조 변경 시 지속적으로 업데이트되어야 합니다.
