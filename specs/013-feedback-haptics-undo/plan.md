# Implementation Plan: Comprehensive Haptic & Visual Feedback with Undo

**Branch**: `013-feedback-haptics-undo` | **Date**: 2026-09-08 | **Spec**: [specs/013-feedback-haptics-undo/spec.md](spec.md)

## Summary

Implement tactile feedback via Jetpack Compose and Android system haptic constants on critical interactions (payment logging, homework completion toggle, segmented button switches, destructive deletions). Introduce actionable Material 3 visual feedback via the root Compose scaffold in `TullabNavGraph` managed by `TullabScaffoldController`, enabling immediate, non-intrusive "Undo" actions for payments, homework completions, and item deletions with in-memory snapshot restoration. Ensure complete trilingual localization parity across English, Turkish, and German without obstructing user navigation.

## Technical Context

**Language/Version**: Kotlin 2.1+, JVM target 17  
**Primary Dependencies**: Jetpack Compose (Material 3, BOM), Hilt, Room, Navigation Compose  
**Storage**: Local SQLite via Room (no schema migration needed; snapshots held in-memory during active Undo window)  
**Testing**: JUnit 4, Kotlin Coroutines Test, Cash App Turbine, Mockito  
**Target Platform**: Android API 26 (Android 8.0) to API 36  
**Project Type**: Mobile Application  
**Performance Goals**: Haptic trigger latency < 16ms, zero UI thread blocking  
**Constraints**: 100% Offline-First (zero internet/external SDKs), trilingual parity (values, values-tr, values-de), accessible touch targets  
**Scale/Scope**: ~6 UI screens/dialogs (Calendar, Homework, Dashboard, Settings, LogLessonDialog, HomeworkBottomSheet)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Principle I: Offline-First & Privacy-First**: PASS. No network calls, telemetry, or third-party cloud services.
- **Principle II: Clean Architecture**: PASS. Pure domain models unaffected; UI layer communicates with repositories/use cases; no Android SDK leakages into domain.
- **Principle III: Dependency Injection via Hilt**: PASS. Existing view models and use cases injected via Hilt.
- **Principle IV: Jetpack Compose-Only & Modern UX**: PASS. Material 3 SnackbarHost, Compose LocalHapticFeedback, accessible touch targets, and dynamic font scale support.
- **Principle V: MVVM & UDF**: PASS. StateFlow for persistent UI state; one-time feedback actions emitted deterministically through scaffold controller / view models.
- **Principle VI: Error Handling**: PASS. Infrastructure exceptions handled safely; operations fail gracefully.
- **Principle VII: Internationalization (i18n)**: PASS. All user-visible strings externalized to English, Turkish, and German with identical resource keys.
- **Principle VIII: Platform Security**: PASS. Zero sensitive information logged; no exported components.

## Project Structure

### Documentation (this feature)

```text
specs/013-feedback-haptics-undo/
├── spec.md              # Feature specification
├── plan.md              # This file (/speckit-plan output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 validation guide
├── contracts/
│   └── feedback-contract.md # UI feedback & haptic interfaces
├── checklists/
│   └── requirements.md  # Quality verification checklist
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (repository root)

```text
app/src/main/
├── java/com/barutdev/tullab/
│   ├── ui/
│   │   ├── components/
│   │   │   └── TullabHaptics.kt          # Haptic feedback utility with API 26-36 support
│   │   ├── navigation/
│   │   │   ├── TullabScaffoldController.kt # Extended with showUndoSnackbar & showMessage
│   │   │   └── TullabNavGraph.kt           # Root Scaffold & SnackbarHost layout
│   │   └── screens/
│   │       ├── calendar/
│   │       │   ├── CalendarScreen.kt       # Mark paid & homework toggle haptics + undo snackbars
│   │       │   └── CalendarViewModel.kt    # Undo payment & homework operations
│   │       ├── homework/
│   │       │   ├── HomeworkScreen.kt       # Homework deletion haptic & undo
│   │       │   ├── HomeworkViewModel.kt    # Undo homework deletion
│   │       │   └── components/
│   │       │       └── HomeworkBottomSheet.kt # Warning haptic & deletion undo
│   │       ├── dashboard/components/
│   │       │   └── LogLessonDialog.kt      # SegmentedButton haptic pulse on change
│   │       └── settings/
│   │           └── SettingsScreen.kt       # CSV export/import snackbar notifications
│   └── res/
│       ├── values/strings.xml              # English strings
│       ├── values-tr/strings.xml           # Turkish strings
│       └── values-de/strings.xml           # German strings
```

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| *None* | N/A | Fully adheres to Constitution and established project patterns |
