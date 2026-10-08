# BLACKBOX Crypto Android v6.4.2

Native Android Kotlin + Jetpack Compose client for BLACKBOX Crypto. The Android app remains server-authoritative: trading, risk, execution and permissions are enforced by the BLACKBOX backend.

## Production build

- Native Android: Kotlin
- UI: Jetpack Compose
- Build system: Gradle Kotlin DSL
- Android Gradle Plugin: 8.7.3
- Kotlin: 2.0.21
- Gradle: 8.11.1
- compileSdk: 35
- targetSdk: 35
- minSdk: 26
- Application ID: `com.blackbox.crypto`
- Release filename: `BLACKBOX-Crypto-Android.apk`

The repository includes `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.properties`. GitHub Actions generates the wrapper JAR on the runner before building; this avoids committing an unverified binary into the source package. Gradle recommends the Wrapper for reproducible builds, and GitHub's Gradle action can install a specific Gradle version when needed.

## Required GitHub Secrets

Create these repository secrets before running **BLACKBOX Android Release APK**:

- `BLACKBOX_API_BASE_URL` — production HTTPS backend URL, for example `https://api.example.com`.
- `ANDROID_KEYSTORE_BASE64` — base64-encoded production Android release keystore.
- `ANDROID_SIGNING_STORE_PASSWORD` — keystore password.
- `ANDROID_SIGNING_KEY_ALIAS` — release key alias.
- `ANDROID_SIGNING_KEY_PASSWORD` — release key password.

**Never commit the keystore, passwords, or private signing material to GitHub.** Keep the same keystore for future releases so Android can accept upgrades over the installed app.

## Build

From GitHub: **Actions → BLACKBOX Android Release APK → Run workflow**.

The workflow installs Android SDK API 35, prepares Gradle 8.11.1, creates the wrapper JAR on the runner, builds a signed release APK, verifies its SHA-256, uploads it as a workflow artifact, and optionally publishes a GitHub Release asset.

## Website distribution

The web dashboard is intended to serve the fixed path:

`/downloads/BLACKBOX-Crypto-Android.apk`

For a self-hosted deployment, place the signed APK at:

`public/downloads/BLACKBOX-Crypto-Android.apk`

Future Android releases keep the same filename. Replace the file with the new signed APK and redeploy the website; the website button/path does not need to change.

## Important

Do not publish an unsigned/debug APK as the production download. Do not change the application ID or release keystore after users have installed a production build, or normal in-place Android upgrades will fail.
