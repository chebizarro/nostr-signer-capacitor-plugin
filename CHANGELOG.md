# Changelog

All notable changes to this project will be documented in this file.

## [0.0.5] - 2025-09-22

### Added
- Android unit tests (Robolectric) covering Content Resolver flows and rejected-provider fallback.
- GitHub Actions workflow at `.github/workflows/ci.yml` to run JS and Android unit tests in CI.
- npm scripts:
  - `test:ts` – run TypeScript/Jest tests.
  - `test:android` – run Android unit tests (`testDebugUnitTest`).
  - `test:all` (default `npm test`) – run both JS and Android tests.

### Changed
- Bumped Android `minSdkVersion` to 23 to align with Capacitor Android requirements.
- Bumped package version to `0.0.5`; Android `versionName` set to `0.0.5` and `versionCode` to `5`.

### Build
- Ensured Gradle runs on JDK 21 and uses `compileSdk 35` / `targetSdk 35`.
- Enabled AndroidX (`android.useAndroidX=true`) and Jetifier (`android.enableJetifier=true`).

---

## [0.0.4] - 2025-xx-xx
- Previous release notes.

