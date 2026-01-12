# TOY Project

Spring Boot 3.3.x, Java 17 기반의 멀티 모듈 프로젝트입니다.

## 프로젝트 구조

```
toy/
├── api/          # REST API 컨트롤러, DTO, 웹 계층
├── core/         # 도메인 엔티티, 비즈니스 로직 (DB 의존성 포함)
└── common/       # 공통 유틸리티, 에러 처리
```

## 기술 스택

- **Spring Boot**: 3.3.7
- **Java**: 17
- **Build Tool**: Gradle (Kotlin DSL)
- **ORM**: Spring Data JPA
- **Database**: MySQL 8.0
- **Cache**: Redis
- **Lombok**: 코드 간소화

## 모듈 설명

### api
- REST API 컨트롤러
- DTO (Data Transfer Object)
- 웹 계층
- 애플리케이션 진입점 (`ToyApplication`)
- `core`, `common` 모듈에 의존

### core
- 도메인 엔티티
- 비즈니스 로직 및 서비스 레이어
- JPA Repository
- DB 의존성 포함 (JPA, MySQL, Redis)
- `common` 모듈에 의존

### common
- 공통 유틸리티 클래스
- 전역 예외 처리 (`GlobalExceptionHandler`)
- 공통 예외 클래스 및 에러 코드
- 독립적인 모듈

## 빌드 및 실행

### 빌드
```bash
./gradlew build
```

### 실행
```bash
./gradlew :api:bootRun
```

### 테스트
```bash
./gradlew test
```

## 데이터베이스 설정

### Docker Compose로 실행
```bash
docker-compose up -d
```

### 연결 정보
- **MySQL**: `localhost:3306`
  - Database: `ticket_service`
  - Username: `root`
  - Password: `root`
- **Redis**: `localhost:6379`
- **RedisInsight**: `http://localhost:8001`

## 프로젝트 구조 상세

- `api` 모듈은 `core`와 `common` 모듈에 의존합니다.
- `core` 모듈은 `common` 모듈에 의존합니다.
- `common` 모듈은 독립적인 모듈입니다.

## 주요 기능

- JPA Auditing (생성일시, 수정일시 자동 관리)
- 전역 예외 처리
- 공통 유틸리티 클래스
- Redis 캐싱 지원
