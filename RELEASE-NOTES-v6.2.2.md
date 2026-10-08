# BLACKBOX Crypto v6.2.2 — Bot & Connection Management

## Included

### 1. Delete Bot — Web + Android
- User-owned bot can be deleted from dashboard and Android.
- Server re-checks the authenticated user and bot ownership.
- Delete is allowed only when the bot is `STOPPED`.
- Delete is blocked if the bot has open positions.
- Delete is blocked if the bot has `PENDING` or `PARTIALLY_FILLED` orders.
- A `BOT_DELETE` audit event is recorded before deletion.
- Web asks for explicit confirmation and warns that bot history is permanently removed.

### 2. Exchange Connect / Disconnect
- Existing connections are shown with their current status.
- Connected exchange rows expose `DISCONNECT`; disconnected rows expose `CONNECT`.
- Disconnect wipes the encrypted API credential (`encryptedRef=null`).
- Real-trading certification metadata is cleared on disconnect.
- A REAL bot that is RUNNING or PAUSED and still uses the exchange blocks disconnect.
- Disconnect is ownership-scoped to the authenticated user.
- Connect/disconnect endpoints have distributed rate limiting with local fallback.
- Withdrawal permission remains permanently forbidden.

### 3. Wallet Connect / Disconnect
- Existing verified wallets show `DISCONNECT`.
- Disconnected wallets show `CONNECT & SIGN`.
- Disconnect is ownership-scoped and only changes the BLACKBOX connection status.
- Seed phrases and private keys are never stored.
- Wallet challenge and verification endpoints are rate-limited.

### 4. Android
- Bot cards include `Delete` when STOPPED.
- Android has a connection management card with native `Disconnect` controls.
- `Connect Exchange` / `Connect Wallet` open the production Connections web flow, preserving provider-specific wallet/API signing UX.
- Android still stores only the authenticated session in encrypted storage and never stores exchange secrets or private keys.

## Security posture

This release adds application-level ownership checks, state checks, credential wiping on exchange disconnect, audit records, and rate limiting around the new destructive/credential-sensitive actions.

It does **not** claim that any software can be guaranteed to have zero vulnerabilities or be immune to DDoS. Production deployment should still use HTTPS, a WAF/CDN, DDoS protection, secure PostgreSQL networking, backups, monitoring, dependency scanning, and independent security testing.

## Validation

- `tests/management-features.test.mjs`: 5/5 passed in the sandbox.
- Full application TypeScript/build validation was not possible in the sandbox because production dependencies/node_modules and Android SDK/Gradle tooling are not installed.
- Real exchange/wallet live E2E was not executed in the sandbox.
- REAL trading flags remain fail-closed by default.
