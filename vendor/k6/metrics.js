function failForNodeRuntime() {
  throw new Error("This script must be run with the k6 CLI, for example: k6 run k6/staged-films-load.js");
}

export class Rate {
  constructor() {
    failForNodeRuntime();
  }
}

export class Trend {
  constructor() {
    failForNodeRuntime();
  }
}
