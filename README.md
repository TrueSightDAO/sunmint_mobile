# sunmint_mobile

SunMint farmer app — native Android/iOS via Capacitor 8 — tree-planting reports with geolocation + tree photos, offline-first queue, email linking, RSA-signed submissions to the TrueSight DAO ledger.

**Relationship to web apps:** `sunmint_beta` (dev) / `sunmint_prod` (prod) host the same web app; this repo is the native wrapper. The web layer lives in `www/` (a **Capacitor-adapted** copy of the web app — it diverges from `sunmint_beta` where native plugins replace web APIs per the plan's PR4–PR7) and the native shells are `android/` and `ios/`.

**Offline note:** the web site's `service-worker.js` does **not** apply to the mobile app. The app's pages ship inside the native bundle (`webDir: www`), so they are always available offline with no service worker; the offline-first submission queue is handled natively by SQLite + Filesystem (IndexedDB fallback only on web).

---

## Architecture

| Concern | Implementation |
|---|---|
| Framework | Capacitor 8 (web layer in `www/`, native shells `android/` + `ios/`) |
| Identity / signing | RSA-2048 keypair (`crypto.subtle`), RSASSA-PKCS1-v1_5 / SHA-256 — **byte-compatible** between web and native (proven in `scripts/rsa-compat-test/`) |
| Key custody | Native secure storage (Android Keystore / iOS Keychain) via `@aparajita/capacitor-secure-storage`; `localStorage` fallback on web |
| Camera | `@capacitor/camera` (camera-only source, JPEG q90) |
| Location | `@capacitor/geolocation` (8s timeout, blank fallback, non-blocking) |
| Offline queue | `@capacitor-community/sqlite` + `@capacitor/filesystem` (photo blobs → Documents/reports); IndexedDB fallback on web |
| Sync triggers | `@capacitor/network` (connectivity change) + `@capacitor/app` (foreground) → flush queue |

---

## Build & release

Prereqs: Node ≥22, JDK 21, Android SDK (build-tools 35, platform android-35). Set `ANDROID_HOME` and `JAVA_HOME`.

```bash
npm install
npx cap sync
```

### Android
```bash
# debug
cd android && ./gradlew assembleDebug        # → android/app/build/outputs/apk/debug/app-debug.apk

# release (signed)
cd android && ./gradlew assembleRelease      # → .../apk/release/app-release.apk
```
Release signing reads `android/keystore.properties` (gitignored). The release keystore is a **credential** — never commit it, never put it in CI logs. Custody protocol: see the plan's `CREDENTIAL_HANDOFF_PROTOCOL.md` (keystore generated at `/opt/android-keystore/sunmint-release.keystore` on the autopilot box; governor to confirm custody/backup).

### iOS
```bash
# REQUIRES A MAC with Xcode (this repo's dev box is Linux — cannot run xcodebuild)
npx cap open ios          # or: cd ios && xcodebuild -workspace App.xcworkspace ...
```
- Bundle ID: `me.truesight.sunmint`
- Deployment target: iOS 15.0
- Info.plist includes camera / photo-library / location usage descriptions
- AppIcon + splash: TrueSight DAO branded (universal 1024px icon, saffron splash)
- **Distribution (TestFlight) requires a provisioned Apple Developer Program account** — provisioning profiles / signing are governor-side (cannot be automated from this box)

---

## Testing

See `sunmint_beta/README.md` testing notes (online submit, offline submit + reconnect flush, retake, "Other" species, email/verification link). The same flows apply to the native app.

## Repo layout
```
www/                    # web app (Capacitor-adapted; diverges from sunmint_beta where native plugins replace web APIs)
android/                # native Android project (Capacitor)
ios/                    # native iOS project (Capacitor)
scripts/rsa-compat-test/ # RSA byte-compatibility proof (Java ↔ Node crypto.subtle)
```
