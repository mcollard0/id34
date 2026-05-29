# id34.java
Modern Java Android rewrite of ID34 with local-first SQLite storage, heatmap-driven idea discovery, and Google Drive cloud backups.

## Highlights
- Single-activity UI for creating, searching, and editing ideas.
- Heatmap bubbles: tap a word to populate the search bar and run search.
- Settings gear (bottom-right) opens backup management.
- Auto-backup on lifecycle (`onPause`/`onStop`) only after data changes.
- Auto-restore from latest cloud backup when local DB is empty.
- Dark mode responsive colors and system bar inset handling.

## Project layout
- `app/` Android application module
- `app/src/main/java/pro/michaelcollard/id34/` application code
- `app/src/main/res/` layouts, drawables, colors, strings
- `build.fish` helper script to build signed debug + release APKs
- `architecture.md` architecture/status notes
- `2.0/` archived legacy project snapshot

## Requirements
- Android SDK installed (platform 35 recommended)
- Java 17+ (build script prefers 21, falls back to 17)
- ADB for device install/testing
- Google Play Services on test device (for Google Sign-In / Drive)

## Build
### Option A: fish helper script (recommended)
- Run `./build.fish`
- Prompts for keystore password once and builds:
  - debug: `app/build/outputs/apk/debug/app-debug.apk`
  - release: `app/build/outputs/apk/release/app-release.apk`

### Option B: direct Gradle
- Debug: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew --no-daemon -Dorg.gradle.java.home=/usr/lib/jvm/java-17-openjdk :app:assembleDebug`
- Release: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew --no-daemon -Dorg.gradle.java.home=/usr/lib/jvm/java-17-openjdk :app:assembleRelease`

## Install on device
- `adb devices`
- `adb -s <device-serial> install -r app/build/outputs/apk/debug/app-debug.apk`
- Launch:
  - `adb -s <device-serial> shell am start -n pro.michaelcollard.id34/.MainActivity`

## Google Drive backup/restore behavior
- Scope: full Drive scope (`https://www.googleapis.com/auth/drive`)
- App folder: `Id34`
- Backup filename format:
  - `backup_cloud_yyyy-MM-ddTHH-mm-ss-SSSZ.sqlite`
  - Example: `backup_cloud_2026-05-29T03-05-00-685Z.sqlite`
- Backup query filter:
  - in `Id34` folder
  - name contains `backup_cloud_`
  - name contains `.sqlite`
  - not trashed
- Restore policy:
  - if local DB has 0 active ideas, restore newest backup (`createdTime desc`, first result)

## Current Android config
- Package: `pro.michaelcollard.id34`
- `compileSdk`: 35
- `targetSdk`: 35
- `minSdk`: 23
- Version name: `3.0.0-beta`

## Notes
- `local.properties` and `backup/` are typically ignored by git.
- If Google Sign-In fails with code 10, verify OAuth Android client SHA fingerprints for the installed signing key.
- AGP may warn for `compileSdk 35` if plugin version is older than tested range; builds can still succeed.
