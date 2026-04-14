//k6 run .\k6\smoke-films.js
//checkt films-endpoint of deze gezond is


import http from 'k6/http';
import { check, sleep } from 'k6';
import { assertK6Runtime, BASE_URL } from './config.js';

assertK6Runtime('k6/smoke-films.js');

export const options = {
  vus: 1,
  iterations: 5,
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

export default function () {
  const response = http.get(`${BASE_URL}/api/films`);

  check(response, {
    'films status is 200': (r) => r.status === 200,
    'films response is json-like': (r) =>
      (r.headers['Content-Type'] || '').includes('application/json'),
  });

  sleep(1);
}
