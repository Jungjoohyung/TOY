# Ticket Service (Toy Project)

## 1. 개발 환경
- Java 17
- Spring Boot 3.3.0 (Multi-module)
- Docker & Docker Compose (MySQL 8.0, Redis 7)

## 2. 프로젝트 실행 방법 (필수)
### 1) DB 실행 (Docker)
`docker-compose up -d`
* MySQL Port: 3306 (Account: root/root)
* DB Name: ticket_service

### 2) 빌드 및 실행
* Core 모듈은 라이브러리이므로 실행 불가능
```powershell
./gradlew clean :core:build :common:build
./gradlew :api:bootRun


## 3. 테스트 UI (브라우저)

앱 실행 후 아래 URL에서 기능 확인용 HTML 콘솔을 사용할 수 있습니다.

```
http://localhost:8080/test.html
```

### 권장 테스트 플로우

| 순서 | 섹션 | 동작 |
|------|------|------|
| 1 | 인증 | 회원가입 → 로그인 (JWT 토큰 자동 저장) |
| 2 | 포인트 | 충전 버튼으로 잔액 확보 |
| 3 | 대기열 | "대기열 등록" → "순번 확인 시작" (3초마다 폴링) |
| 4 | 공연 | "공연 불러오기" → 공연 클릭 → 회차 선택 → "좌석 불러오기" |
| 5 | 예매 | 좌석 클릭(파란색) → "선택 좌석 예매" |
| 6 | 결제 | "최근 예매 결제" 버튼 (예매 직후 활성화됨) |
| 7 | 내역 | "내역 새로고침"으로 확인 / 취소도 가능 |

### UI 구성

- **왼쪽 패널**: 인증 / 대기열 / 포인트 (각 섹션에 응답 로그 콘솔 포함)
- **오른쪽 상단**: 공연 목록 + 회차 선택 (2열 그리드)
- **오른쪽 중간**: 좌석 그리드 (예매된 좌석은 회색으로 비활성화)
- **오른쪽 하단**: 내 예매 내역 테이블 (취소 버튼 포함)

---

## 4. API 엔드포인트 목록

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 반환) |
| POST | `/api/queue` | 대기열 등록 |
| GET | `/api/queue/rank` | 내 대기 순번 조회 |
| GET | `/api/performances` | 공연 목록 조회 |
| GET | `/api/performances/{id}` | 공연 상세 + 회차 목록 |
| GET | `/api/schedules/{scheduleId}/seats` | 좌석 배치도 조회 |
| POST | `/api/reservations` | 좌석 예매 |
| GET | `/api/reservations` | 내 예매 목록 |
| DELETE | `/api/reservations/{id}` | 예매 취소 |
| POST | `/api/payments` | 결제 |
| POST | `/api/points/charge` | 포인트 충전 |