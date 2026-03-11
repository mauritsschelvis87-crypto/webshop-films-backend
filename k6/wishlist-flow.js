import http from 'k6/http';
import { check, sleep } from 'k6';
import { assertK6Runtime, BASE_URL, buildUserEmail, jsonParams } from './config.js';

assertK6Runtime('k6/wishlist-flow.js');

export const options = {
  vus: 1,
  iterations: 2,
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1500'],
  },
};

function createUser(email, password) {
  const response = http.post(
    `${BASE_URL}/api/auth/register`,
    JSON.stringify({ email, password }),
    jsonParams()
  );

  check(response, {
    'user created or already exists': (r) => r.status === 200 || r.status === 409,
  });
}

function getAccountByEmail(email) {
  const response = http.get(`${BASE_URL}/api/account?email=${encodeURIComponent(email)}`);

  check(response, {
    'account lookup succeeded': (r) => r.status === 200,
  });

  return response.json();
}

function getFirstFilmId() {
  const response = http.get(`${BASE_URL}/api/films`);

  check(response, {
    'films lookup succeeded': (r) => r.status === 200,
  });

  const films = response.json();
  if (!Array.isArray(films) || films.length === 0) {
    throw new Error('No films available. Seed data is required for this script.');
  }

  return films[0].id;
}

export default function () {
  const email = buildUserEmail('wishlist');
  const password = 'Test123!';

  createUser(email, password);

  const user = getAccountByEmail(email);
  const filmId = getFirstFilmId();

  const addResponse = http.post(`${BASE_URL}/api/wishlist/${user.id}/add/${filmId}`);
  check(addResponse, {
    'wishlist add status is 200': (r) => r.status === 200,
  });

  const getResponse = http.get(`${BASE_URL}/api/wishlist/${user.id}`);
  check(getResponse, {
    'wishlist get status is 200': (r) => r.status === 200,
    'wishlist contains at least one film': (r) => {
      try {
        const body = r.json();
        return Array.isArray(body) && body.length >= 1;
      } catch {
        return false;
      }
    },
  });

  sleep(1);
}
