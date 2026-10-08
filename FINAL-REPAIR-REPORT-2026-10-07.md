# BLACKBOX Crypto v6 — Final Repair / Hardening Pass

## Scope completed
- Prisma schema formatting repaired and v6 hardening migration added.
- Root Next layout corrected with `<html>` / `<body>`.
- `ccxt` externalized from the Next server bundle.
- Portfolio client boundary repaired.
- Developer login now lands on `/dashboard`; Developer navigation remains available and has the highest operational access.
- Developer/Admin access-code generation and Member redemption retained and hardened with revoke support, tier/expiry/quota and hashed codes.
- Subscription redemption no longer revokes an existing active subscription prematurely; future subscription start is respected.
- Silver/Gold/Platinum market-pair entitlements are 10/50/100; Enterprise is explicitly configured.
- Exchange symbol catalog endpoint added for tier-aware pair selection.
- Candle/MTF analysis is server-side and feeds Signal Engine v2.
- Client-provided score/liquidity/rug-risk is no longer the authority for `/api/signals`.
- Signal lifecycle is persisted; client PATCH can only cancel/expire an owned signal.
- Paper worker uses MTF validation for CEX candidates before entry and persists signal snapshots.
- Real worker path added and remains disabled by default behind environment + exchange-certification gates.
- Real execution now validates KYC/verification, permissions, market precision, exchange-native minimum notional/precision, available balance, allocation and idempotency before order submission (without imposing a platform minimum user balance).
- Network uncertainty after order submission moves the execution to RECONCILE rather than blindly resubmitting.
- Exchange order/balance reconciliation strengthened and reconciliation retention added.
- Session-version invalidation added for admin support.
- MFA re-enrollment requires the current MFA code; OTP verification/resend is rate-limited.
- Registration, setup, bot start, KYC, access redemption and real execution record consent metadata.
- KYC storage receives a persistent Docker volume and `.env` is excluded from the Docker build context.
- Seed no longer overwrites the Developer password on every startup.
- Fake History/Calendar data replaced with database-backed data.
- Portfolio share metrics are now calculated server-side from database records.
- Trade history exposes entry reason, entry/exit prices, size, quantity, duration and full timestamps.
- Dashboard polling relaxed and bot view AI metrics reduced from loading hundreds of rows to aggregates.
- Lightweight CSS motion, hover/press feedback, page entry animation, navigation overflow handling and reduced-motion support added.
- Paper/API Router navigation is horizontally scrollable on narrow screens.
- Log retention expanded to operational/audit/reconciliation tables with configurable retention windows.

## Validation performed in this environment
`npm test`: 45 tests discovered, 44 PASS, 1 SKIP (worker E2E requires installed `ccxt`).

A full `npm run build` / Prisma runtime validation could not be executed in the audit environment because dependencies were not installed and the package registry was not reachable. The source was statically inspected and TypeScript parsing was checked with the available compiler; unresolved diagnostics are dominated by missing installed dependencies/types in this environment.

## Production gate
The package is hardened substantially, but live trading still requires exchange-specific certification using `REAL-TRADING-CERTIFICATION.md`. Keep real flags false until that certification is completed.

## Additional production pass — 2026-10-08
- Fixed exchange order submission lifecycle so the CCXT client is not closed before `createOrder()` resolves.
- Fixed shared Redis rate limiting to use fixed time buckets instead of extending the expiry on every request.
- Access-code redemption no longer revokes a still-active subscription when the new subscription starts in the future.
- Exchange credentials can be connected/tested before live certification; REAL order mode remains certification-gated.
- Manual order cancellation now uses `CANCEL_PENDING` and reconciliation on cancel/fill races instead of assuming cancellation succeeded.
- Added `/api/health` with PostgreSQL health verification and switched Docker healthcheck to that endpoint.
- REAL protective exits may continue during global halt/maintenance; new entries remain blocked. Global API revoke still blocks execution.
- REAL execution rejects expired signals, enforces local-position quantity for SELL, and enforces allocation against actual free quote balance.
- Worker now handles SIGTERM/SIGINT cleanly for VPS/container restarts.
- Added GitHub Actions CI for PostgreSQL + Prisma validation + tests + production build.
- Added `.npmrc` and exact top-level dependency versions to reduce deployment drift.
- Added VPS/GitHub deployment notes and health-check verification.

## Current verification
- `npm test`: 45 tests, 44 PASS, 1 SKIP, 0 FAIL.
- Full Next.js/Prisma production build remains environment-dependent here because the audit sandbox cannot reliably reach the npm registry. CI/Docker will perform dependency installation and the real build in the target environment.
- REAL trading remains intentionally OFF until an exchange-specific certification is completed. This is a safety gate, not a code defect.

## v6.1.1 Railway/VPS crash hardening — 2026-10-08

### Database / Prisma
- Added an idempotent `0001_initial` baseline migration containing the complete Prisma schema instead of assuming old tables already exist.
- Added `0006_schema_repair` for databases previously initialized with `prisma db push`.
- Added `prisma/migrations/migration_lock.toml`.
- Removed production `prisma db push --accept-data-loss` behavior. Production uses `prisma migrate deploy`.
- Completed the `ExchangeAccount.reconciliations` ↔ `Reconciliation` relation and added execution/worker lease metadata.

### Real execution safety
- `executeRealOrderForUser` now hard-rejects unless REAL_TRADING_ENABLED + REAL_TRADING_PRODUCTION_READY are both true.
- Real execution also requires exchange provider certification AND an explicit per-account `realTradingCertifiedAt`.
- Global API revoke always blocks real execution.
- Manual close / close-all cannot bypass the execution gate.
- Partial SELL fills now reduce the local position quantity rather than leaving a phantom full position.
- Per-bot execution lease prevents concurrent workers/requests from double-submitting an order.
- FAILED executions without an external order id can be retried; uncertain orders remain `RECONCILE` and are never blindly resubmitted.

### Futures
- CCXT `defaultType` is explicitly `future` for FUTURES.
- Exchange accounts have an explicit market type and leverage cap (default 1x).
- Futures orders validate the leverage cap and apply the requested leverage before order submission.

### Risk / worker
- REAL worker now persists actual risk telemetry (`realEquity`, `realDayStartEquity`, `realPeakEquity`, `dailyLossPct`, `drawdownPct`).
- New REAL entries are rejected if risk telemetry has not been initialized.
- Worker uses a database lease so multiple Railway replicas cannot independently run the trading loop.
- Worker continues protective exits before the global entry halt check.

### Deployment
- `prisma` and `tsx` are production dependencies so Railway production installs can run migrations/worker without downloading CLIs at runtime.
- Added `scripts/run-production.mjs` to run web + worker together for a single Railway/VPS service.
- Added `railway.json` and health-check deployment configuration.
- CI now runs `prisma migrate deploy` against PostgreSQL before build.

### Verification in this environment
- `npm test`: 44 PASS, 1 SKIP, 0 FAIL.
- Modified TypeScript files passed Node type-strip syntax checking.
- Full `npm run build` and Prisma CLI validation cannot be truthfully claimed here because the sandbox has no installed Prisma CLI/node_modules and npm registry access timed out. GitHub CI is configured to perform the real build/migration verification.

## v6.2 production product hardening — 2026-10-08

### Exchange
- Added explicit BingX catalog entry with Spot + Perpetual Swap support through CCXT.
- Global CEX connection route now accepts the explicit supported catalog, not only Indonesian exchanges.
- Futures adapter uses `swap` for BingX and enforces leverage caps.
- Exchange count is enforced server-side against current tier entitlements.
- Real execution remains certification-gated; an exchange is never considered safe merely because CCXT exposes it.

### Wallets
- Added one-time wallet ownership challenge/verification.
- EVM wallets use EIP-1193 `personal_sign` and server-side signature recovery.
- Phantom uses Phantom Browser SDK injected connection and Solana message signing.
- WalletConnect uses Universal Provider and supports EVM mobile wallet sessions; UI shows a QR/URI handoff.
- Verified wallet records now include verification and last-use timestamps.
- Wallet disconnect/revoke is server-side and audited.
- No seed phrase/private key is accepted or stored.

### Tier / entitlements
- Added `TierUpgradeRequest` and server-side request/approve/reject flow.
- Developer/Admin can create and custom-edit AccessPlan entitlements.
- User can submit tier upgrade requests.
- Entitlements are read dynamically from the active AccessPlan so plan edits do not require rewriting individual users or bots.
- Exchange count is now enforced when connecting new exchange accounts.

### UI motion
- Added explicit start/pause/stop state animations.
- Added signal pulse, risk warning, upgrade success, loading, toast and success/failure motion classes.
- Existing `prefers-reduced-motion` support remains enabled.

### Validation limitation
- Existing automated suite remains 45 tests: 44 pass, 1 skipped, 0 failed.
- Full `npm install` timed out in the sandbox, so a dependency-backed Next/Prisma production build and browser-wallet E2E cannot be honestly claimed as executed here.
- Live exchange certification still requires real/testnet credentials and controlled small-notional tests per exchange. REAL_TRADING flags remain locked until those tests pass.

## v6.2.1 Android Client

Added a native Kotlin + Jetpack Compose Android client under `android/`. It uses the existing production API for authentication, bot control, close-all, signals and risk status. The client enforces HTTPS backend configuration and encrypted session-cookie storage. Real trading remains backend-gated; Android cannot bypass risk, certification, execution locks, or reconciliation. The sandbox did not have a complete Android SDK/Gradle environment, so an APK/AAB build is intentionally not claimed as validated here.

## v6.2.1 Policy Update — Accessible Participation / No User Minimum Balance

At product level, BLACKBOX does not require a minimum user balance/equity to participate in real trading. No `minimumBalance`, `minimumEquity`, or equivalent platform gate is introduced.

The execution layer still enforces exchange-native minimum order/notional, precision, and actual available-balance requirements. Those checks protect order validity and are not a restriction on joining or using BLACKBOX. A small-balance account may remain connected and operational even when a particular order is too small to be accepted by the exchange.

Demo/paper trading may retain virtual-capital presets and simulation constraints separately from the real-user participation policy.
