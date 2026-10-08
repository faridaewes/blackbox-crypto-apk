// PM2 (Windows/Linux VPS tanpa Docker):  pm2 start ecosystem.config.cjs
module.exports = { apps: [
  { name: "blackbox-web", script: "node_modules/next/dist/bin/next", args: "start", env: { NODE_ENV: "production" } },
  { name: "blackbox-worker", script: "node_modules/tsx/dist/cli.mjs", args: "src/worker/paper-engine.ts", env: { NODE_ENV: "production" }, restart_delay: 5000 },
] };
