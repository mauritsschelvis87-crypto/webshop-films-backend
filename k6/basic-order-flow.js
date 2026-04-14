import http from k6/http';
import { check, sleep } from 'k6';
import { assertK6Runtime, BASE_URL, buildUserEmail, jsonParams } from './config.js';

assertK6Runtime('k6/basic-order-flow.js');

export const options = {
  vus: 1,
  iterations: 3,
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1500'],
  },
};

function getFirstFilmId() {
  const filmsResponse = http.get(`${BASE_URL}/api/films`);

  check(filmsResponse, {
    'films fetch succeeded': (r) => r.status === 200,
  });

  const films = filmsResponse.json();
  if (!Array.isArray(films) || films.length === 0) {
    throw new Error('No films available. Seed data is required for this script.');
  }

  return films[0].id;
}

export default function () {
  const filmId = getFirstFilmId();

  const uniqueEmail = buildUserEmail();
  const password = 'Test123!';

  const registerPayload = JSON.stringify({
    email: uniqueEmail,
    password,
  });

  const registerResponse = http.post(
    `${BASE_URL}/api/auth/register`,
    registerPayload,
    jsonParams()
  );

  check(registerResponse, {
    'register returns 200 or 409': (r) => r.status === 200 || r.status === 409,
  });

  const orderPayload = JSON.stringify({
    username: uniqueEmail,
    totalPrice: 19.99,
    items: [
      {
        productId: filmId,
        quantity: 1,
      },
    ],
  });

  const orderResponse = http.post(
    `${BASE_URL}/api/orders`,
    orderPayload,
    jsonParams()
  );

  check(orderResponse, {
    'order status is 200': (r) => r.status === 200,
    'order has id': (r) => {
      try {
        return Boolean(r.json('id'));
      } catch {
        return false;
      }
    },
  });

  sleep(1);
}
