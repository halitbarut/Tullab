# Implementation Plan: UI, UX, and Domain Fixes (Homework, Calendar, Settings)

**Branch**: `011-ui-ux-fixes` | **Date**: 2026-09-08 | **Spec**: [specs/011-ui-ux-fixes/spec.md](file:///home/halit/AndroidStudioProjects/Tullab/specs/011-ui-ux-fixes/spec.md)

**Input**: Feature specification from `/specs/011-ui-ux-fixes/spec.md`

## Summary

Implement targeted UI, UX, and domain improvements across Tullab:
1. Enable permanent homework assignment deletion in `HomeworkBottomSheet` via Room DAO / Repository / ViewModel pipeline with a confirmation dialog.
2. Dynamically adapt the Calendar daily agenda header to the selected day's content ("Lessons on [Date]", "Homework on [Date]", or "Schedule on [Date]").
3. Redesign the homework list empty state to a centered Material 3 layout featuring the Assignment icon, headline, and subtitle matching the student list pattern.
4. Correct layout spacing in Settings notification rows to eliminate label and time value concatenation across font scales.
5. Replace raw ISO date formatting in homework fields with localized medium date formatting based on the user's active locale.
6. Provide full linguistic parity across English (`values`), Turkish (`values-tr`), and German (`values-de`).

## Technical Context

**Language/Version**: Kotlin 2.0.21, JVM target 17  
**Primary Dependencies**: Jetpack Compose, Material 3, Hilt, Room, Navigation Compose  
**Storage**: Room (SQLite)  
**Testing**: JUnit 4, Turbine, kotlinx-coroutines-test, Compose UI Test  
**Target Platform**: Android API 26 (Min SDK) - API 36 (Target SDK)  
**Project Type**: Android Mobile App (Offline-First)  
**Performance Goals**: Smooth 60fps Compose recomposition, instant DB deletions  
**Constraints**: 100% offline-first, no internet permissions, full i18n parity (EN/TR/DE), WCAG AA contrast & 48dp touch targets  
**Scale/Scope**: 5 target modules/screens (`HomeworkBottomSheet`, `HomeworkScreen`, `CalendarScreen`, `SettingsScreen`, `HomeworkRepository`)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Principle I: Offline-First & Privacy-First Architecture**: PASS. All operations run against local Room database; zero network calls.
- **Principle II: Clean Architecture & Meaningful Modularity**: PASS. Dependency flow is `ui -> domain <- data`. Deletion capability is added to `HomeworkDao` (`data`), `HomeworkRepository` (`domain`), `HomeworkRepositoryImpl` (`data`), and consumed by `HomeworkViewModel` (`ui`).
- **Principle III: Dependency Injection via Hilt**: PASS. Repositories and ViewModels use Hilt `@Inject`.
- **Principle IV: Jetpack Compose-Only UI, Accessibility & Modern UX**: PASS. 100% Jetpack Compose / Material 3. 48dp touch targets, semantic content descriptions, scaling up to 200%.
- **Principle V: MVVM, Unidirectional Data Flow & UI Event Channels**: PASS. State hoisted cleanly; event flows and ViewModels handle user actions.
- **Principle VI: Predictable, Layered Error Handling**: PASS. Room coroutine operations handled safely.
- **Principle VII: Internationalization (i18n) & Extensible Formatting**: PASS. All strings externalized with 100% parity across `values`, `values-tr`, and `values-de`. Date/time formatting respects `LocalLocale.current`.
- **Principle VIII: Secrets Management & Android Platform Security**: PASS. No PII logged, no secrets involved.

## Project Structure

### Documentation (this feature)

```text
specs/011-ui-ux-fixes/
├── spec.md              # Feature specification
├── plan.md              # Implementation plan
├── research.md          # Phase 0 decisions and rationale
├── data-model.md        # Phase 1 data access and state changes
├── quickstart.md        # Phase 1 verification and testing guide
├── contracts/           # Component interfaces and UI contracts
│   └── ui-contracts.md
└── checklists/
    └── requirements.md  # Specification quality checklist
```

### Source Code (repository root)

```text
app/src/main/
├── java/com/barutdev/tullab/
│   ├── data/
│   │   ├── local/HomeworkDao.kt
│   │   └── repository/HomeworkRepositoryImpl.kt
│   ├── domain/
│   │   └── repository/HomeworkRepository.kt
│   └── ui/screens/
│       ├── calendar/CalendarScreen.kt
│       ├── homework/
│       │   ├── HomeworkScreen.kt
│       │   ├── HomeworkViewModel.kt
│       │   └── components/HomeworkBottomSheet.kt
│       └── settings/SettingsScreen.kt
└── res/
    ├── values/strings.xml
    ├── values-tr/strings.xml
    └── values-de/strings.xml
```

**Structure Decision**: Standard Tullab Clean Architecture (`app/src/main/java/.../data`, `domain`, `ui`).

## Complexity Tracking

*No violations of the Constitution detected. No complexity exceptions required.*
