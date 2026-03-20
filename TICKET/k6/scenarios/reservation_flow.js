import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, commonHeaders } from '../k6-config.js';

export const options = {
  stages: [
    { duration: '30s', target: 100 },  // Stage 1: 0 -> 100 VU
    { duration: '60s', target: 500 },  // Stage 2: 100 -> 500 VU
    { duration: '60s', target: 1000 }, // Stage 3: 500 -> 1000 VU
    { duration: '60s', target: 1000 }, // Stage 4: Maintain 1000 VU
    { duration: '30s', target: 0 },    // Stage 5: 1000 -> 0 VU
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% of requests must complete below 2s
    http_req_failed: ['rate<0.05'],    // Failure rate must be less than 5%
  },
};

export default function () {
  const userId = Math.floor(Math.random() * 100) + 1;
  const email = `test${userId}@gmail.com`; // Assuming users exist or mock auth is used
  const password = 'password123';

  // 1. Login
  const loginPayload = JSON.stringify({ email, password });
  const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, { headers: commonHeaders });
  check(loginRes, { 'login success': (r) => r.status === 200 });
  
  if (loginRes.status !== 200) {
      sleep(1);
      return;
  }
  
  // Extract JWT token from "data" field (as per ApiResponse structure)
  const loginBody = loginRes.json();
  const token = loginBody.data;

  const authHeaders = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
  };

  // 2. Queue Registration
  const queueRes = http.post(`${BASE_URL}/api/queue`, null, { headers: authHeaders });
  check(queueRes, { 'queue registered': (r) => r.status === 200 });

  // 3. Queue Polling
  let isRankZero = false;
  let attempts = 0;
  while (!isRankZero && attempts < 10) {
    const rankRes = http.get(`${BASE_URL}/api/queue/rank`, { headers: authHeaders });
    check(rankRes, { 'queue rank check success': (r) => r.status === 200 });
    
    if (rankRes.status === 200) {
      const rankData = rankRes.json().data;
      if (rankData === 0 || rankData === null) {
        isRankZero = true;
      }
    }
    
    if (!isRankZero) {
        sleep(1);
        attempts++;
    }
  }

  if (!isRankZero) {
      // Failed to pass queue in time
      return;
  }

  // 4. Reservation (좌석 500개: seatId 1~500)
  const seatId = Math.floor(Math.random() * 500) + 1;
  const reservePayload = JSON.stringify({ seatId: seatId });
  const reserveRes = http.post(`${BASE_URL}/api/reservations`, reservePayload, { 
      headers: authHeaders,
      responseCallback: http.expectedStatuses(200, 409)
  });
  check(reserveRes, { 'reservation success': (r) => r.status === 200 });

  // 5. Payment
  if (reserveRes.status === 200) {
      const reserveData = reserveRes.json().data;
      const reservationId = reserveData; // Assuming it returns reservationId mapped in data
      const paymentPayload = JSON.stringify({ reservationId: reservationId });
      const payRes = http.post(`${BASE_URL}/api/payments`, paymentPayload, { headers: authHeaders });
      check(payRes, { 'payment success': (r) => r.status === 200 });
  }

  sleep(1);
}
