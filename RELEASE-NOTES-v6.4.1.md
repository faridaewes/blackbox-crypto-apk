# BLACKBOX Crypto v6.4.1 — Maintenance Operations & Android Distribution

## Added
- START MAINTENANCE / STOP MAINTENANCE for DEVELOPER and ADMIN.
- Maintenance gate is enabled before bot shutdown cleanup so new worker entry paths are blocked first.
- Automatic stop of RUNNING/PAUSED bots.
- Pending order cancellation attempt: paper/local orders are canceled; real exchange orders are canceled through the existing exchange adapter when possible, with reconciliation remaining authoritative if cancellation is uncertain.
- Persistent database notifications for every ACTIVE account plus best-effort SMTP email.
- Web maintenance overlay/popup and notification center.
- Android maintenance banner and disabled bot controls while maintenance is active.
- ANDROID APP download menu with operator-configured HTTPS APK URL.

## Safety boundary
- Open positions are **not forcibly liquidated** by maintenance. Protective exits and reconciliation remain available as a safety path.
- STOP MAINTENANCE clears only the maintenance flag. Bots stopped by maintenance remain stopped and require explicit user START.
- Existing trading/risk/execution/authentication/database logic remains server-authoritative.
