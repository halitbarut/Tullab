# Implementation Plan: Bulk Lesson Scheduling

**Branch**: `008-bulk-lesson-scheduling` | **Date**: 2026-09-06 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/008-bulk-lesson-scheduling/spec.md`  

## Summary

Add a bulk lesson scheduling feature to Tullab, accessible exclusively within the Student Calendar context (`bulk_schedule/{studentId}`). The feature enables private tutors to plan up to 30 lessons at once using either a Monthly Calendar Grid or a Weekly Routine generator. Lessons inherit the student's saved hourly rate (with an optional custom batch rate override) and unified start times with per-day customization. Conflicting date-time slots for the student are skipped automatically. Lessons are inserted as autonomous individual records in Room, with the newly created IDs held in memory during the completion Snackbar to provide single-tap atomic Undo rollback without schema changes.

---

## Technical Context

**Language/Version**: Kotlin 2.1.10 (JVM Target 17)  
**Primary Dependencies**: Jetpack Compose Material 3 (`androidx.compose.material3`), AndroidX Room 2.7.0, Dagger Hilt 2.55, Kotlinx Coroutines 1.10.1, Jetpack Navigation Compose  
**Storage**: Room SQLite database (`tullab.db`). Schema version remains **10** (no migrations needed; batch IDs are held in memory during Snackbar lifecycle)  
**Testing**: JUnit 4, Cash App Turbine, Kotlinx Coroutines Test (`StandardTestDispatcher`, `runTest`)  
**Target Platform**: Android (Min SDK: 26, Target/Compile SDK: 36)  
**Project Type**: Native Android Application (Clean Architecture: `ui → domain ← data`)  
**Performance Goals**: Candidate generation & collision checks $<50$ms for 30 lessons; instantaneous UI validation feedback ($<16$ms, 60fps)  
**Constraints**: 100% offline-first, zero internet permissions, strict touch target accessibility ($\ge 48\times 48$ dp), full linguistic parity in English (`values`), Turkish (`values-tr`), and German (`values-de`)  
**Scale/Scope**: Up to 30 lessons per batch, tens of students per device, hundreds of lessons in Room database  

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

- [x] **I. Offline-First & Privacy-First Architecture**: Feature operates 100% locally on device using Room; zero network calls or third-party telemetry.
- [x] **II. Clean Architecture & Modularity**: Strict inward dependency flow (`ui → domain ← data`). Domain models (`BulkScheduleDraft`, `BulkScheduleResult`, candidate models) and use cases have zero Android imports.
- [x] **III. Dependency Injection via Hilt**: `BulkScheduleViewModel` is annotated with `@HiltViewModel` and uses `@Inject constructor`. Repositories and use cases bound via Hilt.
- [x] **IV. Jetpack Compose-Only UI & Accessibility**: 100% Compose Material 3. Minimum $48\times 48$ dp touch targets on date cells, day chips, and time buttons. Dynamic font scaling supported without text clipping.
- [x] **V. MVVM & UDF**: `BulkScheduleViewModel` exposes a single immutable `StateFlow<BulkScheduleUiState>`. Composables use `collectAsStateWithLifecycle()`. One-time UI events dispatch via buffered `Channel`.
- [x] **VI. Predictable, Layered Error Handling**: SQLite and data-layer exceptions mapped to typed results. Validation rules (e.g. $>30$ lessons limit) reflected directly in `BulkScheduleUiState`.
- [x] **VII. Internationalization (i18n) & Plurals**: All UI strings and error messages externalized to `strings.xml` for EN, TR, and DE. Created and skipped lesson counts utilize Android `<plurals>` (`getQuantityString` / `pluralStringResource`).
- [x] **VIII. Secrets Management & Platform Security**: Zero PII logged; internal components declared non-exported.
- [x] **Database Evolution**: Verified that no Room schema changes are introduced, leaving schema version at 10.

---

## Project Structure

### Documentation (this feature)

```text
specs/008-bulk-lesson-scheduling/
├── spec.md              # Feature specification with 5 clarified design decisions
├── plan.md              # This implementation plan
├── research.md          # Phase 0 architectural decisions & rationales
├── data-model.md        # Phase 1 domain entities, states, and validation rules
├── quickstart.md        # Phase 1 verification and manual testing guide
├── contracts/           # Phase 1 interface & repository contracts
│   ├── lesson-repository-contract.md
│   ├── bulk-schedule-usecase-contract.md
│   └── ui-navigation-contract.md
└── checklists/
    └── requirements.md  # Specification quality checklist (16/16 passing)
```

### Source Code Layout

```text
app/src/main/
├── java/com/barutdev/tullab/
│   ├── domain/
│   │   ├── model/
│   │   │   └── BulkScheduleModels.kt         # [NEW] BulkScheduleMode, Draft, Candidate, Result
│   │   ├── repository/
│   │   │   └── LessonRepository.kt           # [MODIFY] Add insertLessons, deleteLessons, getLessonDatesForStudent
│   │   └── usecase/lesson/
│   │       ├── CalculateBulkLessonCandidatesUseCase.kt  # [NEW] Evaluates candidates & collisions
│   │       ├── CreateBulkLessonsUseCase.kt              # [NEW] Persists batch to Room
│   │       └── UndoBulkLessonsUseCase.kt                # [NEW] Atomic deletion of batch
│   ├── data/
│   │   ├── local/
│   │   │   └── LessonDao.kt                  # [MODIFY] Add insertLessons, deleteLessons, getLessonDatesForStudent
│   │   └── repository/
│   │       └── LessonRepositoryImpl.kt       # [MODIFY] Implement bulk repository operations
│   ├── navigation/
│   │   ├── TullabDestinations.kt             # [MODIFY] Add BulkSchedule destination
│   │   └── TullabNavGraph.kt                 # [MODIFY] Register BulkSchedule composable route
│   └── ui/screens/
│       ├── calendar/
│       │   ├── CalendarScreen.kt             # [MODIFY] Add TopBar action & handle Undo Snackbar
│       │   └── CalendarViewModel.kt          # [MODIFY] Add undoBulkLessons method
│       └── bulk_schedule/                    # [NEW] Dedicated bulk scheduling screen package
│           ├── BulkScheduleScreen.kt         # [NEW] Root screen composable with tabs & preview
│           ├── BulkScheduleViewModel.kt      # [NEW] MVI/MVVM ViewModel managing draft & candidates
│           ├── BulkScheduleUiState.kt        # [NEW] Immutable UI state and events
│           └── components/
│               ├── CalendarGridSelector.kt   # [NEW] Monthly multi-date selection grid
│               ├── WeeklyRoutineSelector.kt  # [NEW] Weekday toggles & end condition inputs
│               ├── DayTimeChipList.kt        # [NEW] Compact chip list for per-day time overrides
│               ├── BulkSchedulePreviewBar.kt # [NEW] Dynamic preview bar above confirm button
│               └── PastDateWarningDialog.kt  # [NEW] Warning confirmation dialog for past dates
└── res/
    ├── values/strings.xml                    # [MODIFY] Add English strings & <plurals>
    ├── values-tr/strings.xml                 # [MODIFY] Add Turkish translations & <plurals>
    └── values-de/strings.xml                 # [MODIFY] Add German translations & <plurals>
```

---

## Complexity Tracking

> No constitutional violations or unwarranted complexity. Room schema version remains untouched (v10). Undo rollback is handled purely in-memory during the active Snackbar lifecycle.
