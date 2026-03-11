import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { assertK6Runtime, BASE_URL, printJson } from './config.js';

assertK6Runtime('k6/staged-films-load.js');

const filmsDuration = new Trend('films_duration');
const filmsSuccess = new Rate('films_success');

export const options = {
  stages: [
    { duration: '15s', target: 2 },
    { duration: '20s', target: 5 },
    { duration: '15s', target: 10 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1200'],
    films_success: ['rate>0.95'],
    films_duration: ['p(95)<1000'],
  },
};

export default function () {
  const response = http.get(`${BASE_URL}/api/films`);
  filmsDuration.add(response.timings.duration);
  filmsSuccess.add(response.status === 200);

  check(response, {
    'staged films status is 200': (r) => r.status === 200,
  });

  sleep(1);
}

export function handleSummary(data) {
  const summary = {
    checksPassRate: data.metrics.checks ? data.metrics.checks.passes / data.metrics.checks.count : null,
    httpReqFailedRate: data.metrics.http_req_failed ? data.metrics.http_req_failed.rate : null,
    p95DurationMs: data.metrics.http_req_duration ? data.metrics.http_req_duration['p(95)'] : null,
    filmsP95Ms: data.metrics.films_duration ? data.metrics.films_duration['p(95)'] : null,
  };

  printJson('summary', summary);

  return {
    stdout: `\nResult summary\n${JSON.stringify(summary, null, 2)}\n`,
  };
}
