# Quickstart & Verification Guide: Application Rebranding to "Tullab"

**Feature**: `006-rename-app-tullab` | **Date**: 2026-09-05

This guide details runnable verification steps and validation commands to verify that the application has been completely and cleanly rebranded to **"Tullab"**.

---

## Prerequisites

- Android SDK with API level 36 compile target and API 26 minimum.
- Java Development Kit (JDK) 17.
- Working terminal with Gradle wrapper permissions (`./gradlew`).

---

## 1. Automated Verification Suite

Run the full automated test suite to ensure that package namespace relocation and symbol renaming did not introduce any compilation or runtime regressions.

### Unit Tests
```bash
./gradlew testDebugUnitTest
```
**Expected Outcome**: All JVM unit tests pass successfully under package `com.barutdev.tullab`.

### Build & APK Assembly
```bash
./gradlew assembleDebug
```
**Expected Outcome**: Build finishes with `BUILD SUCCESSFUL`. The output APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 2. Package Identity & Manifest Verification

Verify that the compiled APK declares the new application ID and launcher activity:

```bash
./gradlew app:dependencies
```

Inspect Android manifest merged output:
```bash
grep -E 'package|applicationId|TullabApp|Theme.Tullab' app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml
```

**Expected Outcome**:
- Package / Application ID: `com.barutdev.tullab`
- Application class: `com.barutdev.tullab.TullabApp`
- Theme: `@style/Theme.Tullab`

---

## 3. Multilingual String Parity Verification

Verify string resources across all supported language configurations:

```bash
grep -n 'name="app_name"' app/src/main/res/values*/strings.xml
grep -n 'name="onboarding_welcome_title"' app/src/main/res/values*/strings.xml
grep -n 'name="notification_default_message"' app/src/main/res/values*/strings.xml
```

**Expected Outcome**:
- `values/strings.xml`: "Tullab", "Welcome to Tullab", "You have a new reminder from Tullab."
- `values-tr/strings.xml`: "Tullab", "Tullab’a Hoş Geldiniz", "Tullab’dan yeni bir hatırlatmanız var."
- `values-de/strings.xml`: "Tullab", "Willkommen bei Tullab", "Du hast eine neue Erinnerung von Tullab."

---

## 4. Adaptive Launcher Icon Visual Verification

1. Install debug APK on an Android emulator or test device:
   ```bash
   ./gradlew installDebug
   ```
2. Inspect the device Home Screen and Application Drawer:
   - **Label**: Displays "Tullab".
   - **Emblem**: Displays the Tullab crest on a crisp solid white background.
   - **Masking**: Switch launcher mask shapes (Circle, Squircle, Rounded Square in Developer Options / Lawnchair / Pixel Launcher) and verify the emblem remains unclipped.

---

## 5. Backup Export & Legacy Import Walkthrough

1. **Export**:
   - Open Tullab > Settings > Export Data.
   - Verify the suggested filename is `tullab_backup_YYYYMMDD_HHmmss.csv`.
   - Save the file.
2. **Legacy Import**:
   - Rename a backup file or use an existing test file named `kora_backup_20260901_120000.csv`.
   - In Settings > Import Data, select the legacy backup file.
   - Verify that all student, lesson, and payment records import smoothly with a success confirmation.
