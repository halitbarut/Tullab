# Implementation Plan: lesson-times-homework-state

**Branch**: `012-lesson-times-homework-state` | **Date**: 2026-09-08 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/012-lesson-times-homework-state/spec.md`

## Summary

This feature enhances lesson time visibility throughout the app by surfacing device-localized start times across key screens (Calendar daily details, Student Dashboard cards, Log Lesson sheet header). Additionally, it refactors the homework domain and presentation logic by treating "Overdue" strictly as a computed dynamic presentation state (`status == PENDING && dueDate < today`) rather than an editable enum option. "Overdue" is removed from user selection dropdowns, and legacy database records are safely handled via type conversion without requiring a schema version bump.

## Technical Context

**Language/Version**: Kotlin (JVM target 17)
**Primary Dependencies**: Jetpack Compose + Material 3, AndroidX Room, Android Navigation Compose, Hilt
**Storage**: Room (SQLite)
**Testing**: JUnit 4, Cash App Turbine, MockK, Robolectric / AndroidX Test
**Target Platform**: Android (Min SDK 26, Target SDK 36)
**Project Type**: Mobile Application (Android)
**Performance Goals**: Instantaneous UI state derivation without background polling overhead; 60 fps Compose animations.
**Constraints**: 100% offline-capable; zero sensitive logging; localized formatting adhering to device 12/24-hour user preferences.
**Scale/Scope**: Affects Calendar daily details, Student Dashboard, Log Lesson bottom sheet, Homework management screens & dialogs, and Room type converter.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate / Principle | Status | Evaluation |
|---|---|---|
| **Layer Boundaries** (`ui → domain ← data`) | PASS | `HomeworkStatus` and dynamic overdue helpers reside in domain. UI layers strictly format and present state. Data layer provides type conversion without leaking framework details to domain. |
| **Offline Resilience** | PASS | 100% offline operation. All time calculations and status derivations execute on-device. |
| **Localization & Plurals** | PASS | Formatted using `android.text.format.DateFormat.getTimeFormat(context)` and externalized string resources across EN, TR, DE. |
| **Security & Secrets** | PASS | No sensitive student PII logged or exposed. |
| **Compose & Accessibility** | PASS | Material 3 components; icons paired with text or semantic descriptions. Minimum 48x48dp touch targets preserved. |
| **MVVM & UDF** | PASS | State flows unchanged; UI reflects immutable state with computed properties. |
| **Database Evolution** | PASS | No Room schema migration required; `HomeworkStatusConverter` handles legacy `'OVERDUE'` string deserialization gracefully. |
| **Testing Expectations** | PASS | Unit tests covering `Homework.isOverdue()`, `HomeworkStatusConverter`, `CalendarStatusResolver`, and ViewModel toggles. |

## Project Structure

### Documentation (this feature)

```text
specs/012-lesson-times-homework-state/
├── spec.md              # Feature specification
├── plan.md              # Implementation plan
├── research.md          # Research findings and decisions
├── data-model.md        # Updated entity models and converters
├── quickstart.md        # Verification and scenario guide
├── contracts/
│   └── ui-contracts.md  # UI and presentation contracts
├── checklists/
│   └── requirements.md  # Spec quality checklist
└── tasks.md             # Tasks (Phase 2)
```

### Source Code (repository root)

```text
app/src/main/java/com/barutdev/tullab/
├── data/
│   └── local/
│       └── HomeworkStatusConverter.kt          # Maps 'OVERDUE' -> PENDING on read
├── domain/
│   └── model/
│       ├── Homework.kt                         # Adds isOverdue() presentation helper
│       └── HomeworkStatus.kt                   # PENDING, COMPLETED, CANCELLED (removes OVERDUE)
├── ui/
│   ├── screens/
│   │   ├── calendar/
│   │   │   ├── CalendarScreen.kt               # Schedule icon, start time, dynamic overdue badge
│   │   │   ├── CalendarStatusResolver.kt       # Streamlined status resolving
│   │   │   └── CalendarViewModel.kt            # Toggle status without OVERDUE
│   │   ├── dashboard/
│   │   │   ├── DashboardScreen.kt              # Start times in Upcoming, Completed, Past cards
│   │   │   └── components/
│   │   │       └── LogLessonDialog.kt          # Start time in sheet header
│   │   └── homework/
│   │       ├── HomeworkScreen.kt               # Dynamic overdue badge in list and filters
│   │       └── components/
│   │           ├── HomeworkBottomSheet.kt      # Remove OVERDUE from dropdown, show overdue badge
│   │           └── HomeworkDialog.kt           # Remove OVERDUE from dropdown
│   └── util/
│       └── TimeFormatter.kt                    # Centralized localized time formatting helper
└── res/
    └── values*/strings.xml                     # Localization strings for times and badges
```

## Complexity Tracking

> No constitution violations detected. Standard Clean Architecture and Compose patterns applied.
