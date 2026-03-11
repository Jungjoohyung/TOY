import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL } from '../k6-config.js';

export const options = {
  vus: 300,
  duration: '10m', // 10 minutes endurance test
};

export default function () {
  const res = http.get(`${BASE_URL}/api/performances`);
  check(res, { 'status was 200': (r) => r.status === 200 });
  sleep(1);
}
