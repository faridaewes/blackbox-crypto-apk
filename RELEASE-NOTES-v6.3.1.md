# BLACKBOX Crypto v6.3.1 — Motion / UI Polish

## Fix: website animations visibly running

The previous v6.3.0 build contained animation classes and keyframes, but much of the motion was subtle or only triggered by state changes. v6.3.1 adds a dedicated visible motion layer while preserving reduced-motion accessibility behavior.

### Web
- Animated BLACKBOX background grid drift.
- Animated brand status light.
- Hero ambient glow and scan-line effect.
- BLACKBOX bot orb floating/pulse/ring animation.
- START button breathing glow and ring pulse.
- Panel light-sheen animation.
- Active pipeline/status/badge pulse.
- Primary action glow.
- Hover elevation for metrics and controls.
- Dashboard hero state animation is keyed so START/PAUSE/STOP transitions reliably restart.
- Existing event-driven signal/risk/success animations remain intact.

### Android
- Version 6.3.1 / versionCode 631.
- Added visible Compose ambient motion on dashboard.
- Running bot cards receive a subtle pulse.
- Motion remains GPU-friendly and respects accessibility animation settings.

### Validation
- Test suite: 59 PASS, 0 FAIL, 1 SKIPPED.
- Full Next.js production build was not executed in this sandbox because node_modules/Android SDK are unavailable; deploy CI/Docker remains the authoritative build environment.
