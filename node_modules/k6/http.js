const http = {
  get() {
    throw new Error("This script must be run with the k6 CLI, for example: k6 run k6/wishlist-flow.js");
  },
  post() {
    throw new Error("This script must be run with the k6 CLI, for example: k6 run k6/wishlist-flow.js");
  },
};

export default http;
