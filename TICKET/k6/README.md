# 🚀 부하 테스트 & 모니터링 환경 (k6 + Prometheus + Grafana)

본 디렉토리(`k6`)는 티켓팅 시스템의 성능과 병목 지점을 검증하기 위한 부하 테스트 스크립트를 포함하고 있습니다. 이를 통해 **1만 TPS(목표)** 가량의 집중 트래픽 상황에서 시스템의 안정성 및 한계를 측정합니다.

---

## 🏗️ 1. 전제 조건 및 사전 준비
테스트를 시작하기 전에 시스템 인프라와 애플리케이션이 구동되어 있어야 합니다.

1. **인프라 시작 (DB, Redis, Kafka, Prometheus, Grafana)**
   ```bash
   cd TICKET
   docker-compose up -d
   ```
2. **애플리케이션 실행**
   ```bash
   ./gradlew :api:bootRun
   ```

3. **k6 설치 확인**
   OS 환경에 맞는 패키지 매니저로 `k6`가 설치되어 있어야 합니다. (macOS: `brew install k6`, Windows: `winget install k6`)

---

## 🏃 2. 실행 명령어 (k6 Scenarios)

### ① 워밍업 시나리오 (JVM Warm-up)
- **목적**: JVM 웜업, 캐시 및 DB 커넥션 풀 초기 로드. (10 VU, 30초)
```bash
k6 run k6/scenarios/warmup.js
```

### ② 메인 부하 테스트 (단계적 증가)
- **목적**: 최대 1,000개의 VU(가상 유저)가 순차적으로 로그인 → 대기열 통과 → 예약 → 결제 사이클을 반복하여, 실제 서버 병목을 유발하고 안정성을 측정.
```bash
k6 run k6/scenarios/reservation_flow.js --out json=results/result.json
```

### ③ 장기 소크 테스트 (Soak Test)
- **목적**: 300 VU의 지속적인 부하를 10분간 유지하며 시스템 메모리 누수 및 장기적인 커넥션 고갈을 탐지.
```bash
k6 run k6/scenarios/soak_test.js
```

---

## 📊 3. Grafana 대시보드 모니터링
부하 테스트 진행 중 다음 URL에 접속하여 시스템 매트릭을 실시간으로 확인합니다.
* **접속**: `http://localhost:3000`
* **계정**: `admin` / `admin`
* **대시보드 추가 안내**:
  * Prometheus Data Source는 이미 `auto-provisioning` 되어 있습니다.
  * 햄버거 메뉴 → Dashboards → New → Import 메뉴에서 다음 ID를 입력해 대시보드를 쉽게 구성하세요:
    * `4701` : JVM (Micrometer) 매트릭스 상세 보기
    * `10280`: Spring Boot 2.1 System Monitor (또는 `11378`, `12900`)

---

## 🧐 4. 예상 병목 지점 및 확인 방법 (Troubleshooting Guide)

1. **DB 커넥션 풀 고갈 ❗**
   * **원인**: 수천 명의 사용자가 DB 트랜잭션을 쥐고 놓지 않는 상황.
   * **확인**: Grafana `hikaricp.connections.active` 수치가 maximum 설정(예: 10) 꽉 참. 그에 비례해 Pending 커넥션 급증.
   * **개선 포인트**: `application.yml`에서 Hikari `maximumPoolSize` (10 → 50) 조정 및 서비스 내 트랜잭션 보유 시간 단축 적용.

2. **단일 좌석 Redis 락 경합 ❗**
   * **원인**: 특정 몇몇 ID (seatId 1~100 랜덤)로 몰림으로써 발생.
   * **확인**: Spring Actuator의 `http.server.requests` 메트릭에서 `/api/reservations` 레이턴시가 폭발적으로 증가. (예: `p(95) > 2000ms`)
   * **개선 포인트**: 좌석 분산 키 설계/최적화, 혹은 예외 처리 방식 고도화.

3. **Kafka Outbox 테이블 풀스캔 지연 ❗**
   * **원인**: `OutboxRetryScheduler`가 PENDING 내역을 반복 스캔하며 부하 야기 (Index 설정 미흡).
   * **확인**: MySQL Slow Query Log 활성화 시 PENDING 조회 쿼리가 탐지됨.
   * **개선 포인트**: `payment_outbox` 테이블에 `(status, created_at)` 복합 인덱스 검증.
