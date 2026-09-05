# Implementation Plan: Show Homeworks on Calendar

**Branch**: `001-calendar-homeworks` | **Date**: 2026-03-05 | **Spec**: [spec.md](file:///home/halit/AndroidStudioProjects/Kora/specs/001-calendar-homeworks/spec.md)
**Input**: Feature specification from `/specs/001-calendar-homeworks/spec.md`

## Summary

Integrate homework assignments into the Calendar screen with distinct visual identification. The feature displays homework due dates as indicator dots on the monthly calendar (side-by-side with lesson indicators), shows homework details when a day is selected, supports 4 homework statuses (PENDING, COMPLETED, OVERDUE, CANCELLED) with a dedicated color palette (Orange/Teal/Magenta/Gray), and enables status toggling and navigation to the homework edit screen.

**Current state**: Core implementation exists but needs updates to match the clarified spec — particularly the distinct color schema, dual-dot indicators, 4-status enum (currently 3), `CANCELLED` status, component rename, and directed state transition rules.

## Technical Context

**Language/Version**: Kotlin (latest stable), JVM target 17
**Primary Dependencies**: Jetpack Compose + Material 3, Hilt (DI), Jetpack Navigation Compose
**Storage**: Room (local SQLite, offline-only per Constitution Principle I)
**Testing**: JUnit 4 + kotlinx-coroutines-test (unit), compose-ui-test-junit4 (UI)
**Target Platform**: Android API 26+ (Min SDK), API 36 (Target/Compile)
**Project Type**: Mobile app (Android, Jetpack Compose)
**Performance Goals**: < 100ms for day selection with 100+ homework items (SC-004)
**Constraints**: Offline-only, no remote data, all strings in EN/TR/DE (Constitution Principle VI)
**Scale/Scope**: Single-student calendar view, local Room DB

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Privacy-First, Offline-Only | ✅ PASS | Feature is entirely local (Room DB). No network calls. |
| II. Clean Architecture | ✅ PASS | Domain models (`Homework`, `HomeworkStatus`) in `domain/`, Room entities in `data/`, UI in `ui/screens/calendar/`. |
| III. DI via Hilt | ✅ PASS | `CalendarViewModel` uses `@HiltViewModel` + `@Inject constructor`. `HomeworkRepository` injected via Hilt. |
| IV. Jetpack Compose-Only UI | ✅ PASS | All calendar UI is `@Composable`. Material 3 used throughout. |
| V. MVVM with UDF | ✅ PASS | `CalendarViewModel` exposes `StateFlow`, Composables observe state and emit events. |
| VI. i18n Compliance | ✅ PASS | Homework action strings exist in EN, TR, and DE. New strings for `CANCELLED` status and dual-dot indicator will need TR/DE translations. |
| VII. Secrets Management | ✅ PASS (N/A) | No secrets involved in this feature. |

**Gate result**: ✅ All gates pass. Proceeding to Phase 0.

## Project Structure

### Documentation (this feature)

```text
specs/001-calendar-homeworks/
├── plan.md              # This file
├── spec.md              # Feature specification (with Clarifications section)
├── research.md          # Phase 0 output — architectural decisions
├── data-model.md        # Phase 1 output — entity definitions & state machines
├── quickstart.md        # Phase 1 output — implementation status & commands
├── checklists/          # Checklist artifacts
└── tasks.md             # Phase 2 output (from /speckit.tasks)
```

### Source Code (repository root)

```text
app/src/main/java/com/barutdev/kora/
├── domain/model/
│   ├── Homework.kt                          # Domain model (8 fields)
│   ├── HomeworkStatus.kt                    # PENDING, COMPLETED, OVERDUE, CANCELLED
│   └── LessonStatus.kt                     # PAID, COMPLETED, SCHEDULED, CANCELLED
├── domain/repository/
│   └── HomeworkRepository.kt                # Repository interface
├── data/local/dao/
│   └── HomeworkDao.kt                       # Room DAO
├── data/local/entity/
│   └── HomeworkEntity.kt                    # Room entity
├── data/repository/
│   └── HomeworkRepositoryImpl.kt            # Repository implementation
├── ui/theme/
│   └── Color.kt                             # Status colors (add Teal, Magenta, Gray)
├── ui/screens/calendar/
│   ├── CalendarScreen.kt                    # Main screen (DayDetailsSection, HomeworkDetailCard, CalendarDayCell)
│   ├── CalendarStatusResolver.kt            # Dual-dot indicator color resolution
│   └── CalendarViewModel.kt                 # ViewModel (homework fetch, toggle, state transitions)
└── navigation/
    ├── KoraDestinations.kt                  # Homework route with homeworkId
    └── KoraNavGraph.kt                      # NavGraph with homework argument extraction

app/src/main/res/
├── values/strings.xml                       # EN strings
├── values-tr/strings.xml                    # TR strings
└── values-de/strings.xml                    # DE strings

app/src/test/java/com/barutdev/kora/ui/screens/calendar/
├── CalendarStatusLogicTest.kt               # Unit tests for status resolver
└── CalendarViewModelTest.kt                 # ViewModel tests (to be created)
```

**Structure Decision**: Standard Android clean architecture with feature-based screen packages. Calendar feature files are concentrated in `ui/screens/calendar/`. The `CalendarStatusResolver.kt` was extracted from `CalendarScreen.kt` for testability.

## Complexity Tracking

> No constitution violations. No complexity justifications needed.

## Verification Plan

### Automated Tests

| Test | File | Status |
|------|------|--------|
| Status color resolution (14 tests) | `CalendarStatusLogicTest.kt` | ✅ Passing (needs update for dual-dot & new colors) |
| ViewModel homework tests | `CalendarViewModelTest.kt` | ❌ Not yet created |

### Manual Verification Scenarios

| Scenario | Steps | Expected |
|----------|-------|----------|
| MV-001: Dual-dot indicator | Create lesson + homework on same day → View calendar | Two side-by-side dots (left=lesson color, right=homework color) |
| MV-002: Single homework dot | Create homework on day with no lessons → View calendar | Single centered dot in homework color |
| MV-003: Status colors | Create PENDING homework (future) → View calendar | Orange dot for homework |
| MV-004: Overdue detection | Create PENDING homework with past due date → View calendar | Magenta dot (overdue) |
| MV-005: Toggle complete | Mark homework as complete via calendar action | Dot changes to Teal |
| MV-006: Cancel homework | Cancel homework from homework screen → View calendar | Gray dot |
| MV-007: Navigate to edit | Click "Details" on homework card in calendar | Opens homework screen with edit dialog |
| MV-008: Empty state | Select day with no events | "No events scheduled for this day" message |

## Constitution Re-Check (Post-Phase 1 Design)

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Privacy-First, Offline-Only | ✅ PASS | No changes — all data remains local. |
| II. Clean Architecture | ✅ PASS | New `DayIndicators` is in `ui/screens/calendar/` (UI layer). New colors in `ui/theme/`. Enum change in `domain/model/`. Layers respected. |
| III. DI via Hilt | ✅ PASS | No new dependencies needed. Existing Hilt bindings cover `HomeworkRepository`. |
| IV. Jetpack Compose-Only UI | ✅ PASS | All changes are Compose composables. No XML layouts introduced. |
| V. MVVM with UDF | ✅ PASS | State flow unchanged: `CalendarViewModel` → `StateFlow` → Composables → events. |
| VI. i18n Compliance | ⚠️ ACTION | Need to add `CANCELLED` status strings in EN/TR/DE. Addressed in quickstart.md. |
| VII. Secrets Management | ✅ PASS (N/A) | No secrets involved. |

**Post-design gate result**: ✅ All gates pass. i18n for CANCELLED status is tracked as a task item.
