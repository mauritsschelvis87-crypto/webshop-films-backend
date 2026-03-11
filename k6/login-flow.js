import http from 'k6/http';
import { check, sleep } from 'k6';
import { assertK6Runtime, BASE_URL, buildUserEmail, jsonParams } from './config.js';

assertK6Runtime('k6/login-flow.js');

export const options = {
  vus: 1,
  iterations: 3,
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1200'],
  },
};

export default function () {
  const email = buildUserEmail('login');
  const password = 'Test123!';

  const registerResponse = http.post(
    `${BASE_URL}/api/auth/register`,
    JSON.stringify({ email, password }),
    jsonParams()
  );

  check(registerResponse, {
    'register for login test returns 200 or 409': (r) =>
      r.status === 200 || r.status === 409,
  });

  const loginResponse = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email, password }),
    jsonParams()
  );

  check(loginResponse, {
    'login status is 200': (r) => r.status === 200,
    'login response contains email': (r) => {
      try {
        return r.json('email') === email;
      } catch {
        return false;
      }
    },
  });

  sleep(1);
}
