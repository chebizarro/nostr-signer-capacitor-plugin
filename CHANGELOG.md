# Changelog

All notable changes to this project will be documented in this file.

## [0.1.0] - 2026-05-14

### Changed
- Upgraded the plugin to target Capacitor 8.
- Updated Capacitor peer dependency to `>=8.0.0` and added `@capacitor/cli` as a dev dependency.
- Raised documented platform requirements to Node.js 22, Android min SDK 24, and iOS deployment target 15.0.
- Updated the sample app to Capacitor 8 dependencies.
- Bumped Android `versionName` to `0.1.0` and `versionCode` to `6`.

### Fixed
- Corrected Swift Package Manager source and test target paths to match the repository layout.

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
