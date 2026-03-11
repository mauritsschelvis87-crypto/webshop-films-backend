function failForNodeRuntime() {
  throw new Error("This script must be run with the k6 CLI, for example: k6 run k6/wishlist-flow.js");
}

export function check() {
  failForNodeRuntime();
}

export function sleep() {
  failForNodeRuntime();
}
