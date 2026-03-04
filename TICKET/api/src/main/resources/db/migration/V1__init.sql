-- ================================================================
-- V1__init.sql
-- 티켓 예매 시스템 초기 스키마 정의
-- 엔티티 기준: Member, Performance(Concert/Sports), PerformanceSchedule,
--              Seat, Reservation, Payment
-- ================================================================

-- ─────────────────────────────────────────────────────────────────
-- 1. 회원 (Member)
--    - email: 로그인 식별자, UNIQUE
--    - point: 포인트 잔액 (충전/차감), 기본 0
--    - role: USER | ADMIN (EnumType.STRING)
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE member (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    email    VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name     VARCHAR(50)  NOT NULL,
    role     VARCHAR(20),
    point    BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_member       PRIMARY KEY (id),
    CONSTRAINT uq_member_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────
-- 2. 공연 (Performance) — JOINED 상속 전략
--    - dtype: Hibernate 구분자 컬럼 (CONCERT | SPORTS)
--    - 자식 테이블: concert, sports (각각 performance.id를 PK/FK로 공유)
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE performance (
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    dtype VARCHAR(31)  NOT NULL,
    CONSTRAINT pk_performance PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 콘서트 (Concert extends Performance)
CREATE TABLE concert (
    id     BIGINT       NOT NULL,
    artist VARCHAR(100),
    genre  VARCHAR(50),
    CONSTRAINT pk_concert           PRIMARY KEY (id),
    CONSTRAINT fk_concert_perf      FOREIGN KEY (id) REFERENCES performance (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 스포츠 (Sports extends Performance)
CREATE TABLE sports (
    id        BIGINT       NOT NULL,
    home_team VARCHAR(100),
    away_team VARCHAR(100),
    CONSTRAINT pk_sports            PRIMARY KEY (id),
    CONSTRAINT fk_sports_perf       FOREIGN KEY (id) REFERENCES performance (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────
-- 3. 공연 회차 (PerformanceSchedule)
--    - performance와 N:1 관계 (하나의 공연, 여러 회차)
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE performance_schedule (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    performance_id  BIGINT      NOT NULL,
    start_date_time DATETIME(6) NOT NULL,
    CONSTRAINT pk_performance_schedule  PRIMARY KEY (id),
    CONSTRAINT fk_schedule_performance  FOREIGN KEY (performance_id) REFERENCES performance (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────
-- 4. 좌석 (Seat)
--    - schedule_id: 특정 회차에 종속된 좌석
--    - version: 낙관적 락(@Version) 용도
--    - is_reserved: 예약 여부 (기본 false)
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE seat (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    schedule_id BIGINT,
    seat_number VARCHAR(20) NOT NULL,
    price       INT         NOT NULL,
    is_reserved TINYINT(1)  NOT NULL DEFAULT 0,
    version     BIGINT               DEFAULT 0,
    CONSTRAINT pk_seat          PRIMARY KEY (id),
    CONSTRAINT fk_seat_schedule FOREIGN KEY (schedule_id) REFERENCES performance_schedule (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────
-- 5. 예약 (Reservation)
--    - user_id: 비정규화 ID (FK 미설정 — 설계 의도)
--    - seat_id: OneToOne → UNIQUE 제약
--    - status: PENDING | PAID | CANCELLED (EnumType.STRING)
--    - price: 예매 시점 가격 스냅샷
--    - created_at: Spring Auditing @CreatedDate 자동 설정
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE reservation (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    seat_id    BIGINT      NOT NULL,
    price      INT         NOT NULL,
    status     VARCHAR(20) NOT NULL,
    created_at DATETIME(6),
    CONSTRAINT pk_reservation       PRIMARY KEY (id),
    CONSTRAINT uq_reservation_seat  UNIQUE (seat_id),
    CONSTRAINT fk_reservation_seat  FOREIGN KEY (seat_id) REFERENCES seat (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────
-- 6. 결제 (Payment)
--    - reservation_id: OneToOne → UNIQUE 제약
--    - amount: 결제 금액 (예약 시점 좌석 가격)
--    - paid_at: Spring Auditing @CreatedDate 자동 설정
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE payment (
    id             BIGINT NOT NULL AUTO_INCREMENT,
    user_id        BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    amount         INT    NOT NULL,
    paid_at        DATETIME(6),
    CONSTRAINT pk_payment               PRIMARY KEY (id),
    CONSTRAINT uq_payment_reservation   UNIQUE (reservation_id),
    CONSTRAINT fk_payment_reservation   FOREIGN KEY (reservation_id) REFERENCES reservation (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ================================================================
-- SEED DATA
-- ================================================================

-- ─────────────────────────────────────────────────────────────────
-- Concert: 아이유 10주년 콘서트 (50석 VIP, 150,000원)
-- ─────────────────────────────────────────────────────────────────
INSERT INTO performance (title, dtype) VALUES ('아이유 10주년 콘서트', 'CONCERT');
SET @concert_id = LAST_INSERT_ID();
INSERT INTO concert (id, artist, genre) VALUES (@concert_id, '아이유', 'K-POP');

INSERT INTO performance_schedule (performance_id, start_date_time)
VALUES (@concert_id, '2026-08-15 19:00:00.000000');
SET @concert_sched = LAST_INSERT_ID();

INSERT INTO seat (schedule_id, seat_number, price, is_reserved, version) VALUES
(@concert_sched, 'VIP-01', 150000, 0, 0),
(@concert_sched, 'VIP-02', 150000, 0, 0),
(@concert_sched, 'VIP-03', 150000, 0, 0),
(@concert_sched, 'VIP-04', 150000, 0, 0),
(@concert_sched, 'VIP-05', 150000, 0, 0),
(@concert_sched, 'VIP-06', 150000, 0, 0),
(@concert_sched, 'VIP-07', 150000, 0, 0),
(@concert_sched, 'VIP-08', 150000, 0, 0),
(@concert_sched, 'VIP-09', 150000, 0, 0),
(@concert_sched, 'VIP-10', 150000, 0, 0),
(@concert_sched, 'VIP-11', 150000, 0, 0),
(@concert_sched, 'VIP-12', 150000, 0, 0),
(@concert_sched, 'VIP-13', 150000, 0, 0),
(@concert_sched, 'VIP-14', 150000, 0, 0),
(@concert_sched, 'VIP-15', 150000, 0, 0),
(@concert_sched, 'VIP-16', 150000, 0, 0),
(@concert_sched, 'VIP-17', 150000, 0, 0),
(@concert_sched, 'VIP-18', 150000, 0, 0),
(@concert_sched, 'VIP-19', 150000, 0, 0),
(@concert_sched, 'VIP-20', 150000, 0, 0),
(@concert_sched, 'VIP-21', 150000, 0, 0),
(@concert_sched, 'VIP-22', 150000, 0, 0),
(@concert_sched, 'VIP-23', 150000, 0, 0),
(@concert_sched, 'VIP-24', 150000, 0, 0),
(@concert_sched, 'VIP-25', 150000, 0, 0),
(@concert_sched, 'VIP-26', 150000, 0, 0),
(@concert_sched, 'VIP-27', 150000, 0, 0),
(@concert_sched, 'VIP-28', 150000, 0, 0),
(@concert_sched, 'VIP-29', 150000, 0, 0),
(@concert_sched, 'VIP-30', 150000, 0, 0),
(@concert_sched, 'VIP-31', 150000, 0, 0),
(@concert_sched, 'VIP-32', 150000, 0, 0),
(@concert_sched, 'VIP-33', 150000, 0, 0),
(@concert_sched, 'VIP-34', 150000, 0, 0),
(@concert_sched, 'VIP-35', 150000, 0, 0),
(@concert_sched, 'VIP-36', 150000, 0, 0),
(@concert_sched, 'VIP-37', 150000, 0, 0),
(@concert_sched, 'VIP-38', 150000, 0, 0),
(@concert_sched, 'VIP-39', 150000, 0, 0),
(@concert_sched, 'VIP-40', 150000, 0, 0),
(@concert_sched, 'VIP-41', 150000, 0, 0),
(@concert_sched, 'VIP-42', 150000, 0, 0),
(@concert_sched, 'VIP-43', 150000, 0, 0),
(@concert_sched, 'VIP-44', 150000, 0, 0),
(@concert_sched, 'VIP-45', 150000, 0, 0),
(@concert_sched, 'VIP-46', 150000, 0, 0),
(@concert_sched, 'VIP-47', 150000, 0, 0),
(@concert_sched, 'VIP-48', 150000, 0, 0),
(@concert_sched, 'VIP-49', 150000, 0, 0),
(@concert_sched, 'VIP-50', 150000, 0, 0);

-- ─────────────────────────────────────────────────────────────────
-- Sports: FC서울 vs 수원삼성 (50석 A구역, 50,000원)
-- ─────────────────────────────────────────────────────────────────
INSERT INTO performance (title, dtype) VALUES ('FC서울 vs 수원삼성', 'SPORTS');
SET @sports_id = LAST_INSERT_ID();
INSERT INTO sports (id, home_team, away_team) VALUES (@sports_id, 'FC서울', '수원삼성');

INSERT INTO performance_schedule (performance_id, start_date_time)
VALUES (@sports_id, '2026-08-16 18:00:00.000000');
SET @sports_sched = LAST_INSERT_ID();

INSERT INTO seat (schedule_id, seat_number, price, is_reserved, version) VALUES
(@sports_sched, 'A구역-01', 50000, 0, 0),
(@sports_sched, 'A구역-02', 50000, 0, 0),
(@sports_sched, 'A구역-03', 50000, 0, 0),
(@sports_sched, 'A구역-04', 50000, 0, 0),
(@sports_sched, 'A구역-05', 50000, 0, 0),
(@sports_sched, 'A구역-06', 50000, 0, 0),
(@sports_sched, 'A구역-07', 50000, 0, 0),
(@sports_sched, 'A구역-08', 50000, 0, 0),
(@sports_sched, 'A구역-09', 50000, 0, 0),
(@sports_sched, 'A구역-10', 50000, 0, 0),
(@sports_sched, 'A구역-11', 50000, 0, 0),
(@sports_sched, 'A구역-12', 50000, 0, 0),
(@sports_sched, 'A구역-13', 50000, 0, 0),
(@sports_sched, 'A구역-14', 50000, 0, 0),
(@sports_sched, 'A구역-15', 50000, 0, 0),
(@sports_sched, 'A구역-16', 50000, 0, 0),
(@sports_sched, 'A구역-17', 50000, 0, 0),
(@sports_sched, 'A구역-18', 50000, 0, 0),
(@sports_sched, 'A구역-19', 50000, 0, 0),
(@sports_sched, 'A구역-20', 50000, 0, 0),
(@sports_sched, 'A구역-21', 50000, 0, 0),
(@sports_sched, 'A구역-22', 50000, 0, 0),
(@sports_sched, 'A구역-23', 50000, 0, 0),
(@sports_sched, 'A구역-24', 50000, 0, 0),
(@sports_sched, 'A구역-25', 50000, 0, 0),
(@sports_sched, 'A구역-26', 50000, 0, 0),
(@sports_sched, 'A구역-27', 50000, 0, 0),
(@sports_sched, 'A구역-28', 50000, 0, 0),
(@sports_sched, 'A구역-29', 50000, 0, 0),
(@sports_sched, 'A구역-30', 50000, 0, 0),
(@sports_sched, 'A구역-31', 50000, 0, 0),
(@sports_sched, 'A구역-32', 50000, 0, 0),
(@sports_sched, 'A구역-33', 50000, 0, 0),
(@sports_sched, 'A구역-34', 50000, 0, 0),
(@sports_sched, 'A구역-35', 50000, 0, 0),
(@sports_sched, 'A구역-36', 50000, 0, 0),
(@sports_sched, 'A구역-37', 50000, 0, 0),
(@sports_sched, 'A구역-38', 50000, 0, 0),
(@sports_sched, 'A구역-39', 50000, 0, 0),
(@sports_sched, 'A구역-40', 50000, 0, 0),
(@sports_sched, 'A구역-41', 50000, 0, 0),
(@sports_sched, 'A구역-42', 50000, 0, 0),
(@sports_sched, 'A구역-43', 50000, 0, 0),
(@sports_sched, 'A구역-44', 50000, 0, 0),
(@sports_sched, 'A구역-45', 50000, 0, 0),
(@sports_sched, 'A구역-46', 50000, 0, 0),
(@sports_sched, 'A구역-47', 50000, 0, 0),
(@sports_sched, 'A구역-48', 50000, 0, 0),
(@sports_sched, 'A구역-49', 50000, 0, 0),
(@sports_sched, 'A구역-50', 50000, 0, 0);
