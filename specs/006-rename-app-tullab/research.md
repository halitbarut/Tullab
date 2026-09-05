# Research & Architecture Decisions: Application Rebranding to "Tullab"

**Feature**: `006-rename-app-tullab` | **Date**: 2026-09-05

## Summary of Decisions

This document details the architectural decisions and technical implementation strategy for completely renaming the application from "Kora" to "Tullab".

---

### Decision 1: Scope of Codebase Renaming & Package Architecture

- **Decision**: Perform a comprehensive, top-to-bottom project refactoring:
  - Update Gradle `namespace = "com.barutdev.tullab"` and `applicationId = "com.barutdev.tullab"` in `app/build.gradle.kts`.
  - Update `rootProject.name = "Tullab"` in `settings.gradle.kts`.
  - Move package directories from `com/barutdev/kora/` to `com/barutdev/tullab/` across `src/main`, `src/test`, and `src/androidTest`.
  - Rename core application classes and symbols:
    - `KoraApp` → `TullabApp`
    - `KoraTheme` → `TullabTheme`
    - `KoraNavGraph` → `TullabNavGraph`
    - `KoraDestination` → `TullabDestination`
    - `KoraDatabase` → `TullabDatabase`
    - `Theme.Kora` → `Theme.Tullab`
- **Rationale**: The user clarified that the application is currently in pre-release development and has not been published anywhere. A complete rename eliminates lingering legacy naming debt and avoids hybrid identity confusion throughout the codebase.
- **Alternatives Considered**:
  - *Display-only rebranding (keeping `com.barutdev.kora`)*: Evaluated and rejected based on explicit user decision. Display-only rebranding is ideal for live apps with an existing user base to avoid breaking updates, but in pre-release development, clean package naming is superior.

---

### Decision 2: Database Storage & Pre-Release SQLite Strategy

- **Decision**: Update Room database name from `"kora.db"` to `"tullab.db"` in `DatabaseModule.kt` (`DATABASE_NAME = "tullab.db"`).
- **Rationale**: Since the app is in pre-release development, a clean database name matching the application branding prevents orphaned legacy files on test devices and aligns with the clean architecture philosophy.
- **Alternatives Considered**:
  - *Retaining `"kora.db"`*: Rejected because it leaves unnecessary legacy artifacts in internal storage.
  - *Automatic database file copy from `"kora.db"` to `"tullab.db"`*: Unnecessary complexity for an unpublished app, though manual backup export/import allows restoring existing test data.

---

### Decision 3: Adaptive Launcher Icon & Inset Geometry

- **Decision**:
  - Configure `ic_launcher_background.xml` as a solid white vector or color `#FFFFFF`.
  - Use the user-provided vector graphic at `drawable/tullab.xml` as the icon foreground.
  - Wrap `drawable/tullab.xml` within an `<inset>` drawable (e.g. `drawable/ic_launcher_tullab_foreground.xml` with `android:inset="18%"` or `20%`) to ensure the 225.3×268.45dp vector fits safely inside the 72dp adaptive icon safe zone (66% circle) across all Android launcher masks (circle, squircle, rounded rectangle).
  - Update `mipmap-anydpi-v26/ic_launcher.xml` and `mipmap-anydpi-v26/ic_launcher_round.xml` to reference this background and foreground.
- **Rationale**: Android adaptive icons require the central visible content to fit within the 72dp diameter safe viewport of a 108dp canvas. Without proper insetting, vector drawables with tight bounds get clipped along the edges when masked by circular or teardrop launchers.
- **Alternatives Considered**:
  - *Direct foreground reference without insets*: Would cause the top and bottom tips of the geometric emblem to be cropped by circular launcher masks.
  - *Raster PNG generation*: Vector adaptive icons provide crisp rendering across all screen densities without bloating APK size with multiple PNG files.

---

### Decision 4: Multilingual String Localization Matrix

- **Decision**: Update all user-facing branding strings across `values`, `values-tr`, and `values-de` to reflect "Tullab" with correct grammatical inflections:
  - **English (`values/strings.xml`)**:
    - `app_name`: `"Tullab"`
    - `onboarding_welcome_title`: `"Welcome to Tullab"`
    - `notification_default_message`: `"You have a new reminder from Tullab."`
  - **Turkish (`values-tr/strings.xml`)**:
    - `app_name`: `"Tullab"`
    - `onboarding_welcome_title`: `"Tullab’a Hoş Geldiniz"`
    - `notification_default_message`: `"Tullab’dan yeni bir hatırlatmanız var."`
  - **German (`values-de/strings.xml`)**:
    - `app_name`: `"Tullab"`
    - `onboarding_welcome_title`: `"Willkommen bei Tullab"`
    - `notification_default_message`: `"Du hast eine neue Erinnerung von Tullab."`
- **Rationale**: Satisfies Constitution Principle VII (Internationalization & Linguistic Parity) with accurate local grammar.
- **Alternatives Considered**:
  - *Hardcoding "Tullab" in English only*: Rejected; violates Constitution Principle VII.

---

### Decision 5: Backup Export & Backward Compatible Import Strategy

- **Decision**:
  - In `SettingsScreen.kt`, update default export filename from `"kora_backup_..."` to `"tullab_backup_${LocalDateTime.now().format(backupFileNameFormatter)}.csv"`.
  - In `DataBackupManager.kt`, ensure CSV parsing operates purely on file content structure (columns, types) without validating filename prefixes.
- **Rationale**: Guarantees that historical backup files created during testing under the previous name (`kora_backup_*.csv`) can still be selected and restored without issues.
- **Alternatives Considered**:
  - *Strict filename prefix enforcement*: Rejected; would break user ability to restore previously exported backup files.
