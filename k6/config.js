const env = globalThis.__ENV ?? {};
const vu = globalThis.__VU ?? 0;
const iter = globalThis.__ITER ?? 0;

export const BASE_URL = env.BASE_URL || 'http://localhost:8080';

export const defaultHeaders = {
  'Content-Type': 'application/json',
};

export function jsonParams(extraHeaders = {}) {
  return {
    headers: {
      ...defaultHeaders,
      ...extraHeaders,
    },
  };
}

export function assertK6Runtime(scriptPath) {
  if (!globalThis.__ENV) {
    throw new Error(`This script must be run with the k6 CLI, for example: k6 run ${scriptPath}`);
  }
}

export function buildUserEmail(prefix = 'k6-user') {
  return `${prefix}-${vu}-${iter}@example.com`;
}

export function printJson(label, value) {
  console.log(`${label}: ${JSON.stringify(value)}`);
}
