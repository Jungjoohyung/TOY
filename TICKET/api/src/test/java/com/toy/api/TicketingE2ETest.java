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

        // DB 정리
        reservationRepository.deleteAllInBatch();
        seatRepository.deleteAllInBatch();
        scheduleRepository.deleteAllInBatch();
        performanceRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        // 1. 공연 및 스케줄, 좌석 세팅
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

        // [2] 로그인 -> JWT 토큰(회원 ID 문자열 형태) 획득
        LoginRequest loginRequest = new LoginRequest("testuser@gmail.com", "password123");
        String loginResponse = given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .extract().asString(); 

        // 응답: "로그인 성공! 회원 ID: {token}"
        String token = loginResponse.replaceAll("[^0-9a-zA-Z\\-._]", ""); 
        // 실제 프로젝트에선 JWT 토큰이 발급됨. 여기서는 응답 텍스트를 파싱해서 처리
        if (loginResponse.contains("ID: ")) {
            token = loginResponse.substring(loginResponse.indexOf("ID: ") + 4).trim();
        }
        assertThat(token).isNotBlank();

        // [3] 대기열 진입 토큰 발급
        String queueResponse = given()
            .header("Authorization", "Bearer " + token)
        .when()
            .post("/api/queue")
        .then()
            .statusCode(200)
            .extract().asString();

        assertThat(queueResponse).contains("대기열 등록 완료");
        
        // 🚨 큐 강제 활성화 (테스트를 위해)
        Long userId = jwtUtil.getUserId(token);
        queueRepository.activate(userId);
        
        // 큐 토큰은 Interceptor에서 Auth 헤더의 userId로 활성화 여부를 검색하므로
        // 더ਮੀ 값이라도 Queue-Token 헤더가 들어가 있어야 합니다. (헤더 유무 검사)
        String queueToken = "dummy-queue-token";

        // [4] 보유 포인트 충전
        ChargeRequest chargeRequest = new ChargeRequest(200000); // 20만원 충전
        String chargeResponse = given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(chargeRequest)
        .when()
            .post("/api/points/charge")
        .then()
            .statusCode(200)
            .extract().asString();
            
        assertThat(chargeResponse).contains("200000");

        // [5] 예약 진행 (결제 포함)
        ReservationRequest reservationRequest = new ReservationRequest();
        // 리플렉션을 사용해 은닉된 필드 세팅 (단순 테스트용)
        try {
            java.lang.reflect.Field field = ReservationRequest.class.getDeclaredField("seatId");
            field.setAccessible(true);
            field.set(reservationRequest, seatId);
        } catch (Exception e) {}

        String reserveResponse = given()
            .header("Authorization", "Bearer " + token)
            .header("Queue-Token", queueToken)
            .contentType(ContentType.JSON)
            .body(reservationRequest)
        .when()
            .post("/api/reservations")
        .then()
            .statusCode(200)
            .extract().asString();

        assertThat(reserveResponse).contains("예매 성공");

        // [6] 예약 내역 조회 검증 (여기서는 JSON 배열 리턴됨)
        given()
            .header("Authorization", "Bearer " + token)
            .header("Queue-Token", queueToken)
        .when()
            .get("/api/reservations")
        .then()
            .statusCode(200)
            .body("size()", is(1))
            .body("[0].seatNumber", equalTo("VIP-1"));
        
        // [7] 잔액 차감 확인 (20만 - 15만 = 5만) /point/balance가 없으므로 생략
    }

    @Test
    @DisplayName("🚨 동시성 테스트: 10명이 동시에 같은 좌석 예매 시도 시 1명만 성공해야 한다.")
    public void concurrentReservationTest() throws InterruptedException {
        int threadCount = 10;
        java.util.concurrent.ExecutorService executorService = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);

        // 1. 10명의 유저 생성 및 로그인, 큐 통과
        String[] tokens = new String[threadCount];
        for (int i = 0; i < threadCount; i++) {
            String email = "concurrent" + i + "@gmail.com";
            SignupRequest signupRequest = new SignupRequest(email, "password", "테스터" + i, com.toy.core.domain.member.MemberRole.USER);
            given().contentType(ContentType.JSON).body(signupRequest).post("/api/auth/signup");

            String loginResp = given().contentType(ContentType.JSON).body(new LoginRequest(email, "password")).post("/api/auth/login").asString();
            String token = loginResp.substring(loginResp.indexOf("ID: ") + 4).trim();
            tokens[i] = token;

            // 큐 강제 통과
            queueRepository.activate(jwtUtil.getUserId(token));
            
            // 포인트 충전
            ChargeRequest chargeRequest = new ChargeRequest(200000);
            given().header("Authorization", "Bearer " + token).contentType(ContentType.JSON).body(chargeRequest).post("/api/points/charge");
        }

        // 2. 동시 예매 시작
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

        // 3. 1명만 성공하고 9명은 실패해야 함 (400 or 409 등)
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

        // JWT (Authorization) 헤더 없이 요청
        given()
            .header("Queue-Token", "dummy")
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/reservations")
        .then()
            .statusCode(not(200)); // 400, 401, 500 중 하나
    }

    @Test
    @DisplayName("🚨 예외 테스트: 대기열(Queue) 통과(Active) 없이 예약 접근 시 실패해야 한다.")
    public void queueTokenNotActivatedTest() throws Exception {
        // [1] 가입 및 로그인
        SignupRequest signupRequest = new SignupRequest("invalidQueue@gmail.com", "password123", "테스터", com.toy.core.domain.member.MemberRole.USER);
        given().contentType(ContentType.JSON).body(signupRequest).post("/api/auth/signup");

        String loginResp = given().contentType(ContentType.JSON).body(new LoginRequest("invalidQueue@gmail.com", "password123")).post("/api/auth/login").asString();
        String token = loginResp.substring(loginResp.indexOf("ID: ") + 4).trim();

        // 🚨 큐 활성화를 하지 않음 (queueRepository.activate 호출 안 함)

        ReservationRequest req = new ReservationRequest();
        java.lang.reflect.Field field = ReservationRequest.class.getDeclaredField("seatId");
        field.setAccessible(true);
        field.set(req, seatId);

        // JWT 토큰은 있지만 큐 액티브 토큰이 없는 상태
        given()
            .header("Authorization", "Bearer " + token)
            .header("Queue-Token", "dummy")
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/reservations")
        .then()
            .statusCode(not(200)); // 400, 403, 500 중 하나
    }
}
