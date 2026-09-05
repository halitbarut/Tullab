# Implementation Plan: Lesson Rate Management, Multi-Rate Payment Breakdown, and Calendar Rate Editing

**Branch**: `005-lesson-rate-fixes` | **Date**: 2026-09-04 | **Spec**: [specs/005-lesson-rate-fixes/spec.md](file:///home/halit/AndroidStudioProjects/Kora/specs/005-lesson-rate-fixes/spec.md)
**Input**: Feature specification from `specs/005-lesson-rate-fixes/spec.md`

## Summary

This feature resolves three critical usability, accounting, and internationalization defects in lesson rate management:
1. **Context-Aware Rate Change Dialog & In-App Localization**: Accurately categorizes uncompleted scheduled lessons into past, future, or mixed relative to current date, adapting dialog messaging accordingly, and switches string resolution to `koraStringResource` to honor the in-app language (`values`, `values-tr`, `values-de`).
2. **Multi-Rate Payment Cycle Breakdown**: Groups completed lessons awaiting payment by rate tier, computing and rendering a responsive two-column breakdown on the Dashboard (e.g., `5 hours × 200 TL` on the left, `1,000 TL` on the right) that mathematically matches the total amount due.
3. **Calendar Scheduled Lesson Rate Editing**: Replaces the disabled "Complete Lesson" button in the future scheduled lesson dialog with an active "Save Changes" action, allowing tutors to edit and persist updated rates while retaining `SCHEDULED` status.

## Technical Context

**Language/Version**: Kotlin (pinned in `gradle/libs.versions.toml`), JVM Target 17  
**Primary Dependencies**: Jetpack Compose + Material 3, AndroidX Lifecycle (`collectAsStateWithLifecycle`), Hilt (`@HiltViewModel`), Room (SQLite), Coroutines / Flow  
**Storage**: Room local database (`KoraDatabase`) offline persistence  
**Testing**: JUnit 4, Kotlin Coroutines Test (`runTest`), Cash App Turbine  
**Target Platform**: Android API 26+ (Target/Compile SDK 36)  
**Project Type**: Android Mobile App (Single module `:app`)  
**Performance Goals**: Instant UI response (<16ms frame budgeting), deterministic grouping of completed lessons  
**Constraints**: 100% offline-first, no `INTERNET` permission, strict Clean Architecture (`ui → domain ← data`), Compose-only Material 3, complete i18n parity in EN, TR, DE  
**Scale/Scope**: 3 screens (`EditStudentProfileScreen`, `DashboardScreen`, `CalendarScreen`), 2 shared dialogs (`ScheduledLessonsRatePromptDialog`, `LogLessonDialog`), localized string tables across 3 languages  

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
| :--- | :---: | :--- |
| **I. Offline-First & Privacy-First** | **PASS** | 100% local Room storage; no network calls; zero PII logging. |
| **II. Clean Architecture & Modularity** | **PASS** | UI layer consumes domain models and repository contracts; no framework leaks into domain. |
| **III. Hilt Dependency Injection** | **PASS** | ViewModels use `@HiltViewModel` and `@Inject constructor`. |
| **IV. Compose-Only & Modern UX** | **PASS** | Pure Material 3 Composables; responsive wrapping; minimum 48dp touch targets. |
| **V. MVVM, UDF & UI Event Channels** | **PASS** | ViewModels expose immutable `StateFlow<UiState>`; events dispatched via members; transient side-effects via buffered `Channel`. |
| **VI. Predictable Error Handling** | **PASS** | Repositories return safe results; ViewModels handle fallback states cleanly. |
| **VII. i18n & Extensible Formatting** | **PASS** | All new strings in `strings.xml` across EN, TR, and DE; `koraStringResource` used to honor `LocalLocale`. |
| **VIII. Platform Security** | **PASS** | No secrets; no exported components; offline storage. |

## Project Structure

### Documentation (this feature)

```text
specs/005-lesson-rate-fixes/
├── plan.md              # This implementation plan
├── research.md          # Phase 0 decisions & alternatives
├── data-model.md        # Phase 1 data models & state transitions
├── quickstart.md        # Phase 1 validation scenarios
├── contracts/           # Phase 1 UI & ViewModel contracts
│   ├── ui-dialogs.md
│   ├── dashboard-breakdown-contract.md
│   └── viewmodel-contracts.md
└── checklists/
    └── requirements.md  # Spec quality checklist
```

### Source Code Impact (repository root)

```text
app/src/main/
├── java/com/barutdev/kora/
│   ├── ui/screens/
│   │   ├── student_profile/
│   │   │   ├── ScheduledLessonsScope.kt (or within StudentProfileUiState.kt)
│   │   │   ├── EditStudentProfileViewModel.kt
│   │   │   └── components/
│   │   │       └── ScheduledLessonsRatePromptDialog.kt
│   │   ├── dashboard/
│   │   │   ├── PaymentBreakdownTier.kt (or within DashboardUiState.kt)
│   │   │   ├── DashboardViewModel.kt
│   │   │   ├── DashboardScreen.kt
│   │   │   └── components/
│   │   │       └── LogLessonDialog.kt
│   │   └── calendar/
│   │       ├── CalendarViewModel.kt
│   │       └── CalendarScreen.kt
└── res/
    ├── values/strings.xml
    ├── values-tr/strings.xml
    └── values-de/strings.xml

app/src/test/java/com/barutdev/kora/
├── ui/screens/
│   ├── student_profile/EditStudentProfileViewModelTest.kt
│   ├── dashboard/DashboardViewModelTest.kt
│   └── calendar/CalendarViewModelTest.kt
```

**Structure Decision**: Standard Android single-module Clean Architecture repository structure. All changes cleanly integrate into existing UI, ViewModel, and resource components without introducing unnecessary abstractions or breaking backward compatibility.

## Complexity Tracking

> **No constitutional violations exist. All gates pass.**

| Concern | Status | Rationale |
| :--- | :---: | :--- |
| **Schema Changes** | **None** | Existing `LessonEntity` schema already stores `pricingMode` and `rateOrFee`. No Room schema migration required. |
| **Dependency Additions** | **None** | All required utilities (`koraStringResource`, coroutines, compose-material3) are already in place. |
