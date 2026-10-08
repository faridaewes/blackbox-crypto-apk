# BLACKBOX Crypto v6.0

AI Trading Intelligence Platform — **paper trading** (simulasi) dengan arsitektur
`SCAN → THINK → RISK → ROUTE → FILL → MONITOR`.

> AI mengusulkan. Risk Engine memutuskan. Execution Engine mengeksekusi.

## Yang berfungsi
- **Wizard setup 4 langkah** (Mode & Modal, AI Engine, Guardrails, Review & Launch) dengan persetujuan risiko setiap START.
- **Paper engine** (worker): memindai pasar (DexScreener, fallback simulasi), meminta keputusan AI, melewatkan **risk gate**, mensimulasikan rute/slippage/fee, membuka–menutup posisi (SL/TP/breakeven), menghitung equity, drawdown, loss harian, dan **mode trailing** saat target sesi tercapai.
- **Dashboard live** (equity curve, target/peak/batas DD, exposure, pipeline, kecepatan 1x/3x/10x), **Screener** multi-chain, **Trades** (posisi, CLOSE/CLOSE ALL, riwayat), **AI Brain** (agent.log, metrik panggilan/token/latensi/biaya, uji koneksi), **Settings** (ubah guardrails saat bot jalan).
- **AI**: OpenRouter / Groq (OpenAI-compatible) / Ollama lokal / mesin aturan lokal — dengan pembatas kuota untuk free tier.
- **Admin**: STOP ALL BOTS, GLOBAL API REVOKE (frasa konfirmasi), maintenance mode, pengguna & paket (tier), audit log. RBAC Developer/Admin/User; sesi diverifikasi ke database di setiap request.
- **Keamanan**: aplikasi menolak start di production dengan rahasia lemah; kredensial exchange dienkripsi AES-256-GCM; REAL trading terkunci; tidak ada penyimpanan seed phrase/private key.

## Mulai
Lihat **INSTALL.md** (Docker: `node scripts/setup.mjs` → `docker compose up -d --build`).

## Struktur
`src/lib/paper/` mesin simulasi · `src/worker/` loop worker · `src/lib/ai.ts` adapter AI · `src/lib/risk.ts`, `trading-mode.ts` gerbang risiko · `src/app/api/` API · `src/app/dashboard/` UI · `tests/` 34 tes otomatis · `docs/` spesifikasi dan arsitektur.

Status verifikasi dan batasan: **RELEASE-AUDIT.md**. Ini bukan nasihat investasi; trading kripto berisiko tinggi.

## v5.4 upgrade notes
This package is the merged BLACKBOX Crypto v5.3 + patch 5.3.1 master with the requested production-oriented additions: access-code generation/redeem, tiered market limits, CCXT candle analysis, detailed trade history, lightweight motion UI, legal consent audit trail, email/SMS verification hooks, KYC submission/review, support tickets, bounded worker logs, and stricter AI timeouts.

For production, configure PostgreSQL, SMTP, Twilio, a private KYC document storage/provider, HTTPS, and have all legal/AML/KYC documents reviewed for the applicable Indonesian business structure and licensing.


## v6.0 architecture update
- Hybrid Signal Engine v2: multi-timeframe alignment, market regime, signal lifecycle and performance history.
- Execution safety: PRECHECK → SUBMITTING → ACK/PARTIAL/FILLED → RECONCILE, idempotency keys, cancel and reconciliation endpoints.
- REAL mode remains fail-closed unless both `REAL_TRADING_ENABLED=true` and `REAL_TRADING_PRODUCTION_READY=true` are explicitly configured.
- Email + phone verification, optional TOTP MFA, backtest API, risk status API, live signal SSE, and optional market-intelligence provider.
- Production still requires exchange-specific sandbox certification, PostgreSQL migration, dependency lockfile, observability, and legal/compliance review.

## v6 hardened real-trading gate
Real trading is intentionally disabled by default. Read `REAL-TRADING-CERTIFICATION.md` before enabling `REAL_TRADING_ENABLED` and `REAL_TRADING_PRODUCTION_READY`. The exchange must also be listed in `REAL_CERTIFIED_EXCHANGES` after exchange-specific sandbox/small-notional certification.


## Production VPS / GitHub

1. Push the repository to GitHub.
2. On the VPS, install Docker + Docker Compose.
3. Copy `.env.example` to `.env` by running `npm run setup` once on the host, or create the required secrets in the VPS environment.
4. Run `docker compose up -d --build`.
5. Verify `GET /api/health` returns `status=healthy`.
6. Keep `REAL_TRADING_ENABLED=false` and `REAL_TRADING_PRODUCTION_READY=false` until the exchange-specific certification checklist is complete.

The application can run in paper mode without real-execution credentials. Exchange credentials may be connected/tested, but real order submission remains certification-gated.

### Linux VPS quick deploy

```bash
git clone <YOUR_GITHUB_REPOSITORY>
cd BLACKBOX-Crypto
npm run setup
./scripts/deploy-vps.sh
```

For HTTPS, put Nginx/Caddy/Cloudflare in front of port 3000. Do not expose PostgreSQL port 5432 publicly.

### GitHub

Commit the repository including `.github/workflows/ci.yml`. The CI job provisions PostgreSQL, validates Prisma, runs the automated test suite, and attempts the production Next.js build.

## Railway deployment — v6.1.1

1. Create a PostgreSQL service and set `DATABASE_URL` from Railway.
2. Deploy this repository as a Docker service. `railway.json` points to `Dockerfile` and the health endpoint.
3. Set production secrets from `.env.example`. Keep `REAL_TRADING_ENABLED=false` and `REAL_TRADING_PRODUCTION_READY=false`.
4. The container runs `prisma migrate deploy`, then the idempotent seed, then web + worker via `npm run start:all`.
5. Verify `https://YOUR-DOMAIN/api/health` returns `{"ok":true,"status":"healthy"...}`.
6. Do not run `prisma db push --accept-data-loss` on the Railway database.
7. For real trading, complete the exchange certification checklist first. Add the provider to `REAL_CERTIFIED_EXCHANGES`, complete the controlled certification procedure with `REAL_CERTIFICATION_MODE=true`, certify the specific account, and only then enable REAL trading flags for the controlled test window.


## v6.2 production hardening
- BingX added to the explicit CEX catalog with Spot + Perpetual Swap support through CCXT; real execution remains certification-gated.
- Wallet ownership is now verified with one-time server challenges and signatures. MetaMask/EIP-1193, Phantom Browser SDK, and WalletConnect Universal Provider flows are wired. BLACKBOX never stores seed phrases/private keys.
- Developer/Admin can customize AccessPlan entitlements and approve/reject user tier-upgrade requests.
- Added WalletChallenge and TierUpgradeRequest migrations.
- Added explicit interaction motion states for bot start/pause/stop, signals, risk alerts, upgrade success, loading and toast feedback.
- Full real-trading certification still requires live exchange credentials, sandbox/testnet checks where supported, and small-notional production verification per exchange.

## v6.2.2 Native Android Client

`android/` contains the native Kotlin/Jetpack Compose Android client. It connects to the same BLACKBOX v6.2 backend and keeps trading/risk/execution server-authoritative. The client stores only the authenticated session cookie in Android encrypted storage and never stores exchange secrets or wallet private keys.

See `android/ANDROID-README.md` for build and deployment instructions.

## Participation Policy — No Platform Minimum Balance

BLACKBOX Crypto does **not** impose a platform-level minimum user balance for participation in the real trading program. Users may connect an eligible exchange account and use the platform regardless of account size.

Important distinction:
- **No BLACKBOX minimum balance:** there is no product rule such as `minimumBalance`, `minimumEquity`, or `minimumSaldoUser` that blocks participation.
- **Exchange-native minimum order rules remain enforced:** an exchange may require a minimum notional, quantity, lot size, price precision, or available balance for a particular order. These are technical execution constraints from the exchange, not a BLACKBOX participation fee/barrier.
- If a user's available balance is too small for a specific exchange order, BLACKBOX should safely skip/reject that order and keep the account/bot eligible rather than blocking the user from the platform.
- **Demo/Paper:** virtual capital presets may retain their own simulation constraints because they are not real user funds.

This policy is intentional: maximize accessibility while preserving exchange-required execution safety.


## ALMAI — Penasihat Berjangka

Dashboard user Web dan Android menyediakan menu **Penasihat Berjangka** yang membuka referral resmi ALMAI: `https://almai.id/referral/Indonesia`. Menu menggunakan identitas/logo ALMAI dan hanya mengarahkan pengguna ke situs eksternal resmi; proses pendaftaran, KYC, pembayaran, dan layanan ALMAI tetap berlangsung di sistem ALMAI.


## v6.2.2 — Bot & Connection Management
- User dapat menghapus bot dari web dan Android; server hanya mengizinkan penghapusan saat bot STOPPED tanpa posisi terbuka/order aktif.
- Exchange memiliki CONNECT/DISCONNECT; disconnect menghapus encrypted API credential dari database dan diblokir bila connection masih dipakai bot REAL aktif.
- Wallet memiliki CONNECT/DISCONNECT; disconnect mencabut status koneksi tanpa menyentuh aset wallet.
- Endpoint disconnect diberi rate limit terdistribusi bila Upstash Redis tersedia, dengan fallback limiter lokal.
- Android menyediakan kontrol disconnect native dan tombol connect yang membuka flow Connections web untuk provider wallet/exchange.

## v6.3.1 — Security, sessions, tier & bootstrap
- Safe **HAPUS BOT** now archives the bot so trading/audit history remains available.
- Security Center: MFA, password change, active sessions/devices, session revoke and security activity.
- Exchange/wallet CONNECT and DISCONNECT require MFA step-up.
- Active redeemed **Tier** is shown on the Access page and user dashboard; Android also displays it.
- Reconciliation worker checks remote open orders/positions where supported and records discrepancies.
- Health endpoint reports database + worker lease status.
- Backup/DR and external penetration-test runbooks included.
- Android 6.3.0 adds native exchange connection form plus connection/security controls.

### First-install bootstrap accounts
The production seed creates these accounts **only when they do not already exist**:
- Developer: `developer@blackbox.com` / `@blackbox123`
- Admin: `admin@blackbox.com` / `@adminblackbox123`

Both accounts are marked `mustChangePassword=true`, so the first login is redirected to Security Center. Change both passwords immediately and enable MFA. For a public GitHub/production deployment, set `DEVELOPER_SEED_PASSWORD` and `ADMIN_SEED_PASSWORD` environment variables to private strong values before the first seed.


## v6.3.1 Motion / UI Polish
Website and Android now include a visible, GPU-friendly motion layer for the BLACKBOX interface, while respecting reduced-motion accessibility preferences. See `RELEASE-NOTES-v6.3.1.md`.

## v6.4.0 — COMMAND CENTER MOTION UI
Web and Android now share a BLACKBOX command-center presentation layer inspired by the supplied motion reference: larger terminal controls, SVG navigation, animated BLACKBOX CORE, event-driven signal/pipeline motion, live event cards, stronger target/risk transitions, and mobile command navigation. This release intentionally changes presentation/interaction only; trading, risk, AI, execution, authentication, exchange/wallet APIs, database and workers remain server-authoritative and unchanged.


## v6.4.2 — Self-Hosted Android APK Distribution

- Developer dan Admin memiliki **START MAINTENANCE** dan **STOP MAINTENANCE** dengan konfirmasi server-side.
- START MAINTENANCE mengaktifkan `MAINTENANCE_MODE` terlebih dahulu, menghentikan bot `RUNNING/PAUSED`, membatalkan order pending yang dapat dibatalkan, dan memblokir entry baru.
- Semua akun ACTIVE menerima persistent in-app notification dan best-effort email melalui SMTP. STOP MAINTENANCE mengirim notifikasi/email kebalikan dan tidak auto-restart bot.
- Open positions tidak dipaksa close saat maintenance; protective exits dan reconciliation tetap menjadi safety path.
- Website memiliki maintenance warning/popup dan notification center. Android menampilkan maintenance banner dan menonaktifkan kontrol bot selama maintenance.
- Menu **ANDROID APP** disediakan untuk download mandiri dari path tetap `/downloads/BLACKBOX-Crypto-Android.apk`. Tidak diperlukan environment variable atau perubahan code saat update APK; cukup ganti file APK dengan nama yang sama.
- Trading/risk/execution/authentication/database worker tetap server-authoritative; fitur ini tidak memindahkan decision logic ke UI.

## Android Release Build (GitHub Actions)

The Android project is Native Kotlin + Jetpack Compose and is prepared for a cloud-only GitHub Actions release build. Use `.github/workflows/android-release.yml` and configure the required repository secrets documented in `android/ANDROID-README.md`.

The workflow provisions Android SDK API 35 and Gradle 8.11.1, generates the official Gradle Wrapper files on the runner, restores the protected release keystore from GitHub Secrets, injects the production HTTPS API URL, builds a signed release APK, verifies SHA-256, and outputs `BLACKBOX-Crypto-Android.apk`.
