# BLACKBOX Crypto v6.4.0 — Command Center Motion UI

## Scope
v6.4.0 is a presentation/interaction layer update for the existing BLACKBOX Crypto web dashboard and native Android client. The trading engine, risk engine, AI confirmation, execution gates, authentication, exchange/wallet APIs, database schema and worker behavior are intentionally unchanged.

## Command Center UI
- Larger command-oriented navigation with SVG icons and active-state glow.
- BLACKBOX CORE visual with layered rings, scan effect, ambient breathing and event-state treatment.
- START / PAUSE / RESUME control is visually emphasized without changing its existing server action.
- Execution pipeline is presented as sequential command stages.
- Logs are surfaced as event cards with staged entry motion.
- Existing target/profit modal and risk notifications receive stronger command-center presentation.
- Mobile bottom navigation remains horizontally scrollable and keeps the existing routes.

## Android
- Native Compose command-center dashboard with the same visual language as web.
- Animated BLACKBOX core, status chips, bot fleet cards, live signal cards and connection/security panels.
- Existing backend API calls remain the source of truth.
- No private keys or exchange secrets are persisted in the APK.

## Safety
- Motion uses transform/opacity-oriented effects and respects `prefers-reduced-motion` on web.
- No trading logic, risk thresholds, execution permissions, tier enforcement or credentials were changed.
- Real trading remains subject to the existing production certification gates.
