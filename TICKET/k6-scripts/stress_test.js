import http from 'k6/http';
import { check, sleep } from 'k6';

// ⚙️ 테스트 설정 (미사일 스펙)
export const options = {
    // 단계별 부하 테스트
    stages: [
        { duration: '10s', target: 50 },  // 처음 10초 동안 50명까지 서서히 증가 (Warm-up)
        { duration: '30s', target: 500 }, // 30초 동안 500명 유지 (본격 부하)
        { duration: '10s', target: 0 },   // 10초 동안 0명으로 감소 (Cool-down)
    ],
};

// 🏃‍♂️ 가상 유저가 할 행동
export default function () {
    const url = 'http://host.docker.internal:8080/api/queue'; // 도커에서 로컬호스트 접근용 주소

    // 1. 헤더 설정 (토큰 필요하면 여기에 추가, 지금은 테스트라 생략 가능하거나 가짜 토큰 사용)
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJ0ZXN0QHRlc3QuY29tIiwiaWF0IjoxNzY5Njc3NDczLCJleHAiOjE3Njk2ODEwNzN9.bcwtptG9a1tZ83GAsqeXKYZAkPiRZQHdA9zhOu9NiHQ', // 필요 시 주석 해제
        },
    };

    // 2. 대기열 등록 요청 (POST)
    // 매번 다른 유저인 척하기 위해 랜덤 ID를 헤더나 바디에 넣을 수도 있지만,
    // 지금은 단순 트래픽 양을 보는 거라 그냥 쏩니다.
    const res = http.post(url, null, params);

    // 3. 결과 확인 (잘 들어갔나?)
    check(res, {
        'status is 200': (r) => r.status === 200,
        'status is 500': (r) => r.status === 500, // 서버 터지면 500
    });

    // 0.1초 ~ 1초 랜덤 대기 (사람처럼 행동)
    sleep(Math.random() * 1);
}