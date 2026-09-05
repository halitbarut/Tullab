# Implementation Plan: Application Rebranding to "Tullab"

**Branch**: `006-rename-app-tullab` | **Date**: 2026-09-05 | **Spec**: [spec.md](file:///home/halit/AndroidStudioProjects/Kora/specs/006-rename-app-tullab/spec.md)  
**Input**: Feature specification from `/specs/006-rename-app-tullab/spec.md`

## Summary

Completely rebrand the application from "Kora" to "Tullab" across the entire codebase. As clarified by the user, because the app is in pre-release development and unpublished, the rename encompasses the Android application ID (`com.barutdev.tullab`), package namespace, Room database name (`tullab.db`), and internal class/theme symbols (`TullabApp`, `TullabTheme`, `TullabDatabase`). In addition, the app's adaptive launcher icon is updated to feature the custom vector emblem (`drawable/tullab.xml`) centered on a solid white background, all user-facing strings are localized across English, Turkish, and German, and backup export filenames adopt the `tullab_backup_` prefix with continued backward-compatible import support.

## Technical Context

**Language/Version**: Kotlin 2.1.10 (JVM Target 17)  
**Primary Dependencies**: Jetpack Compose Material 3, AndroidX Room 2.7.0, Dagger Hilt 2.55, Kotlinx Coroutines 1.10.1  
**Storage**: Room SQLite database (`tullab.db`)  
**Testing**: JUnit 4, Cash App Turbine, Kotlinx Coroutines Test, Compose UI Test JUnit 4  
**Target Platform**: Android (Min SDK: 26, Target/Compile SDK: 36)  
**Project Type**: Native Android Application (Clean Architecture: `ui → domain ← data`)  
**Performance Goals**: 60 fps smooth rendering, instant startup  
**Constraints**: 100% offline-first, no network permissions, full linguistic parity (EN, TR, DE), seamless backup backward compatibility  
**Scale/Scope**: Entire application codebase refactored to `com.barutdev.tullab`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

- [x] **I. Offline-First & Privacy-First Architecture**: 100% offline operation preserved; Room database renamed to `tullab.db`; zero network permissions or telemetry.
- [x] **II. Clean Architecture & Modularity**: Dependency flow remains `ui → domain ← data`; package namespace consistently mapped to `com.barutdev.tullab.*`.
- [x] **III. Dependency Injection via Hilt**: `@HiltAndroidApp` on `TullabApp`; `DatabaseModule` provides `TullabDatabase` and DAOs cleanly.
- [x] **IV. Jetpack Compose-Only UI & Accessibility**: 100% Jetpack Compose with Material 3; root theme updated to `TullabTheme`; adaptive launcher icon with white background and safe inset margin.
- [x] **V. MVVM & UDF**: UI architecture and state flows remain unchanged.
- [x] **VI. Predictable Error Handling**: Unchanged; safe typed results across boundaries.
- [x] **VII. Internationalization & Formatting**: String parity across `values`, `values-tr`, and `values-de` strictly maintained with correct localized grammatical inflections.
- [x] **VIII. Secrets & Security**: Zero secrets in source code; release shrinking preserved.

## Project Structure

### Documentation (this feature)

```text
specs/006-rename-app-tullab/
├── plan.md              # This implementation plan
├── research.md          # Phase 0 architectural decisions & rationales
├── data-model.md        # Phase 1 branding profile, icon model, and storage specs
├── quickstart.md        # Phase 1 verification and manual test walkthrough
├── contracts/           # Phase 1 interface & resource contracts
│   ├── branding-contract.md
│   └── backup-contract.md
├── checklists/
│   └── requirements.md  # Specification quality checklist (16/16 passed)
└── spec.md              # Feature specification
```

### Source Code Layout

```text
root/
├── settings.gradle.kts                         # [MODIFY] rootProject.name = "Tullab"
app/
├── build.gradle.kts                            # [MODIFY] namespace and applicationId = "com.barutdev.tullab"
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml                 # [MODIFY] TullabApp, Theme.Tullab
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── strings.xml                 # [MODIFY] app_name, welcome, notifications
│   │   │   │   └── themes.xml                  # [MODIFY] Theme.Tullab
│   │   │   ├── values-tr/
│   │   │   │   └── strings.xml                 # [MODIFY] Turkish copy
│   │   │   ├── values-de/
│   │   │   │   └── strings.xml                 # [MODIFY] German copy
│   │   │   ├── drawable/
│   │   │   │   ├── ic_launcher_background.xml  # [MODIFY] Solid white #FFFFFF
│   │   │   │   ├── ic_launcher_tullab_foreground.xml # [NEW] Inset wrapper for drawable/tullab.xml
│   │   │   │   └── tullab.xml                  # [EXISTING] User-provided vector emblem
│   │   │   └── mipmap-anydpi-v26/
│   │   │       ├── ic_launcher.xml             # [MODIFY] Bind white bg + tullab foreground
│   │   │       └── ic_launcher_round.xml       # [MODIFY] Bind white bg + tullab foreground
│   │   └── java/com/barutdev/tullab/           # [MOVE & RENAME] from com/barutdev/kora/
│   │       ├── TullabApp.kt                    # [RENAME] from KoraApp.kt
│   │       ├── MainActivity.kt                 # [MODIFY] TullabTheme, TullabNavGraph imports
│   │       ├── di/
│   │       │   └── DatabaseModule.kt           # [MODIFY] TullabDatabase, DATABASE_NAME = "tullab.db"
│   │       ├── data/
│   │       │   └── local/
│   │       │       └── TullabDatabase.kt       # [RENAME] from KoraDatabase.kt
│   │       ├── navigation/
│   │       │   ├── TullabNavGraph.kt           # [RENAME] from KoraNavGraph.kt
│   │       │   └── TullabDestination.kt        # [RENAME] from KoraDestination.kt
│   │       └── ui/
│   │           ├── theme/
│   │           │   └── Theme.kt                # [MODIFY] TullabTheme
│   │           └── screens/settings/
│   │               └── SettingsScreen.kt       # [MODIFY] tullab_backup_ filename pattern
│   ├── test/java/com/barutdev/tullab/          # [MOVE & MODIFY] Package declarations & imports
│   └── androidTest/java/com/barutdev/tullab/   # [MOVE & MODIFY] Package declarations & imports
```

## Complexity Tracking

*No constitutional violations identified. No complexity justifications required.*
