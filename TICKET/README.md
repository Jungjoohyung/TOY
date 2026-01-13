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


##### 3. API TEST 예제
 Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/performances" -ContentType "application/json; charset=utf-8" -Body '{"name": "Psy Concert", "place": "Seoul Stadium", "price": 150000, "startDate": "2024-07-20", "endDate": "2024-07-21"}'