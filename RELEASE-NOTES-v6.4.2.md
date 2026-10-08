# BLACKBOX Crypto v6.4.2 — Fixed Self-Hosted Android APK Distribution

- Android download URL is fixed to `/downloads/BLACKBOX-Crypto-Android.apk`.
- No `ANDROID_DOWNLOAD_URL` or `NEXT_PUBLIC_ANDROID_DOWNLOAD_URL` environment variable is required.
- Future Android releases require only replacing the APK file with the same filename.
- The website code and download link do not need to change.
- The signed production APK must be placed at `public/downloads/BLACKBOX-Crypto-Android.apk` before packaging/deployment.
