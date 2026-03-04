package com.toy.api;

import com.toy.api.controller.dto.ChargeRequest;
import com.toy.api.controller.dto.LoginRequest;
import com.toy.api.controller.dto.SignupRequest;
import com.toy.core.domain.member.MemberRepository;
import com.toy.core.domain.performance.Concert;
import com.toy.core.domain.performance.PerformanceRepository;
import com.toy.core.domain.performance.PerformanceSchedule;
import com.toy.core.domain.performance.PerformanceScheduleRepository;
import com.toy.core.domain.reservation.ReservationRepository;
import com.toy.core.domain.seat.Seat;
import com.toy.core.domain.seat.SeatRepository;
import com.toy.core.domain.reservation.dto.ReservationRequest;
import com.toy.core.domain.queue.QueueRepository;
import com.toy.core.config.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TicketingE2ETest {

    @LocalServerPort
    private int port;

    @Autowired private MemberRepository memberRepository;
    @Autowired private PerformanceRepository performanceRepository;
    @Autowired private PerformanceScheduleRepository scheduleRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private QueueRepository queueRepository;
    @Autowired private JwtUtil jwtUtil;

    private Long scheduleId;
    private Long seatId;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;

        reservationRepository.deleteAllInBatch();
        seatRepository.deleteAllInBatch();
        scheduleRepository.deleteAllInBatch();
        performanceRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        Concert concert = Concert.builder()
                .title("아이유 10주년 콘서트")
                .artist("아이유")
                .genre("K-POP")
                .build();
        performanceRepository.save(concert);

        PerformanceSchedule schedule = PerformanceSchedule.builder()
                .performance(concert)
                .startDateTime(LocalDateTime.now().plusDays(7))
                .build();
        scheduleRepository.save(schedule);
        scheduleId = schedule.getId();

        Seat seat = Seat.builder()
                .seatNumber("VIP-1")
                .price(150000)
                .schedule(schedule)
                .build();
        seatRepository.save(seat);
        seatId = seat.getId();
    }

    @Test
    @DisplayName("✅ 대기열 발급 -> 좌석 선택 -> 예약 -> 결제 전체 흐름 E2E 테스트")
    public void ticketingFullFlowTest() {
        // [1] 회원 가입
        SignupRequest signupRequest = new SignupRequest("testuser@gmail.com", "password123", "테스터유저", com.toy.core.domain.member.MemberRole.USER);
        given()
            .contentType(ContentType.JSON)
            .body(signupRequest)
        .when()
            .post("/api/auth/signup")
        .then()
            .statusCode(200);

        // [2] 로그인 → ApiResponse.data 에서 JWT 토큰 추출
        String token = given()
            .contentType(ContentType.JSON)
            .body(new LoginRequest("testuser@gmail.com", "password123"))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .extract().path("data");

        assertThat(token).isNotBlank();

        // [3] 대기열 진입
        String queueMessage = given()
            .header("Authorization", "Bearer " + token)
        .when()
            .post("/api/queue")
        .then()
            .statusCode(200)
            .extract().path("message");

        assertThat(queueMessage).contains("대기열 등록 완료");

        // 큐 강제 활성화 (테스트용)
        Long userId = jwtUtil.getUserId(token);
        queueRepository.activate(userId);

        String queueToken = "dummy-queue-token";

        // [4] 포인트 충전 → ApiResponse.data 에서 잔액 추출
        long balance = given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(new ChargeRequest(200000))
        .when()
            .post("/api/points/charge")
        .then()
            .statusCode(200)
            .extract().jsonPath().getLong("data");

        assertThat(balance).isEqualTo(200000L);

        // [5] 예약 진행
        ReservationRequest reservationRequest = new ReservationRequest();
        try {
            java.lang.reflect.Field field = ReservationRequest.class.getDeclaredField("seatId");
            field.setAccessible(true);
            field.set(reservationRequest, seatId);
        } catch (Exception e) {}

        String reserveMessage = given()
            .header("Authorization", "Bearer " + token)
            .header("Queue-Token", queueToken)
            .contentType(ContentType.JSON)
            .body(reservationRequest)
        .when()
            .post("/api/reservations")
        .then()
            .statusCode(200)
            .extract().path("message");

        assertThat(reserveMessage).contains("예매 성공");

        // [6] 예약 내역 조회 → ApiResponse.data 배열에서 확인
        given()
            .header("Authorization", "Bearer " + token)
            .header("Queue-Token", queueToken)
        .when()
            .get("/api/reservations")
        .then()
            .statusCode(200)
            .body("data.size()", is(1))
            .body("data[0].seatNumber", equalTo("VIP-1"));
    }

    @Test
    @DisplayName("🚨 동시성 테스트: 10명이 동시에 같은 좌석 예매 시도 시 1명만 성공해야 한다.")
    public void concurrentReservationTest() throws InterruptedException {
        int threadCount = 10;
        java.util.concurrent.ExecutorService executorService = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);

        String[] tokens = new String[threadCount];
        for (int i = 0; i < threadCount; i++) {
            String email = "concurrent" + i + "@gmail.com";
            SignupRequest signupRequest = new SignupRequest(email, "password", "테스터" + i, com.toy.core.domain.member.MemberRole.USER);
            given().contentType(ContentType.JSON).body(signupRequest).post("/api/auth/signup");

            // ApiResponse.data 에서 JWT 토큰 추출
            String token = given()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(email, "password"))
            .when()
                .post("/api/auth/login")
            .then()
                .extract().path("data");

            tokens[i] = token;

            queueRepository.activate(jwtUtil.getUserId(token));

            given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(new ChargeRequest(200000))
                .post("/api/points/charge");
        }

        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger failCount = new java.util.concurrent.atomic.AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            final String token = tokens[i];
            executorService.submit(() -> {
                try {
                    ReservationRequest req = new ReservationRequest();
                    java.lang.reflect.Field field = ReservationRequest.class.getDeclaredField("seatId");
                    field.setAccessible(true);
                    field.set(req, seatId);

                    Response res = given()
                        .header("Authorization", "Bearer " + token)
                        .header("Queue-Token", "dummy")
                        .contentType(ContentType.JSON)
                        .body(req)
                    .when()
                        .post("/api/reservations");

                    if (res.statusCode() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);
    }

    @Test
    @DisplayName("🚨 예외 테스트: JWT 토큰 없이 접근하면 예약이 실패해야 한다.")
    public void unauthorizedAccessTest() throws Exception {
        ReservationRequest req = new ReservationRequest();
        java.lang.reflect.Field field = ReservationRequest.class.getDeclaredField("seatId");
        field.setAccessible(true);
        field.set(req, seatId);

        given()
            .header("Queue-Token", "dummy")
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/reservations")
        .then()
            .statusCode(not(200));
    }

    @Test
    @DisplayName("🚨 예외 테스트: 대기열(Queue) 통과(Active) 없이 예약 접근 시 실패해야 한다.")
    public void queueTokenNotActivatedTest() throws Exception {
        SignupRequest signupRequest = new SignupRequest("invalidQueue@gmail.com", "password123", "테스터", com.toy.core.domain.member.MemberRole.USER);
        given().contentType(ContentType.JSON).body(signupRequest).post("/api/auth/signup");

        // ApiResponse.data 에서 JWT 토큰 추출
        String token = given()
            .contentType(ContentType.JSON)
            .body(new LoginRequest("invalidQueue@gmail.com", "password123"))
        .when()
            .post("/api/auth/login")
        .then()
            .extract().path("data");

        // 큐 활성화 없음

        ReservationRequest req = new ReservationRequest();
        java.lang.reflect.Field field = ReservationRequest.class.getDeclaredField("seatId");
        field.setAccessible(true);
        field.set(req, seatId);

        given()
            .header("Authorization", "Bearer " + token)
            .header("Queue-Token", "dummy")
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/reservations")
        .then()
            .statusCode(not(200));
    }
}
